package edu.madrasati.sim;
//Fuad Alawi  
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Madrasati Reliability Simulation");
        int nodes = 5;
        int requests = 10000;
        double baseFailureRate = 0.01;
        int chaosEvery = 300;

        for (String arg : args) {
            if (arg.startsWith("--nodes=")) {
                nodes = Integer.parseInt(arg.substring(8));
            } else if (arg.startsWith("--requests=")) {
                requests = Integer.parseInt(arg.substring(11));
            } else if (arg.startsWith("--failure=")) {
                baseFailureRate = Double.parseDouble(arg.substring(10));
            } else if (arg.startsWith("--chaosEvery=")) {
                chaosEvery = Integer.parseInt(arg.substring(13));
            } else if (arg.startsWith("--chaos=")) {
                chaosEvery = Integer.parseInt(arg.substring(8));
            }
        }

        List<ServiceNode> pool = new ArrayList<>();
        for (int i = 0; i < nodes; i++) {
            pool.add(new ServiceNode("svc-" + i, baseFailureRate));
        }

        LoadBalancer lb = new LoadBalancer(pool);
        ChaosMonkey chaos = new ChaosMonkey(pool);
        Metrics metrics = new Metrics();

        for (int i = 1; i <= requests; i++) {
            if (i % chaosEvery == 0) {
                chaos.introduceChaos();
            }
            Request req = new Request(UUID.randomUUID().toString());
            long start = System.nanoTime();
            boolean ok = lb.handle(req);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            metrics.record(ok, latencyMs);
        }

        metrics.print();
    }

    static class Request {
        final String id;
        Request(String id) { this.id = id; }
    }

    static class ServiceNode {
        final String name;
        volatile boolean healthy = true;
        volatile double failureRate;

        ServiceNode(String name, double failureRate) {
            this.name = name;
            this.failureRate = failureRate;
        }

        boolean handle(Request r) {
            long latency = ThreadLocalRandom.current().nextLong(10, healthy ? 60 : 200);
            try { Thread.sleep(latency / 10); } catch (InterruptedException ignored) {}

            double p = ThreadLocalRandom.current().nextDouble();
            double effectiveFailureRate = healthy ? failureRate : Math.min(1.0, failureRate + 0.3);
            if (p < effectiveFailureRate) {
                return false;
            }
            return true;
        }
    }

    static class CircuitBreaker {
        enum State { CLOSED, OPEN, HALF_OPEN }
        private State state = State.CLOSED;
        private final int failureThreshold;
        private final long openMillis;
        private int failures = 0;
        private long openedAt = 0;

        CircuitBreaker(int failureThreshold, long openMillis) {
            this.failureThreshold = failureThreshold;
            this.openMillis = openMillis;
        }

        boolean allow() {
            long now = System.currentTimeMillis();
            switch (state) {
                case CLOSED: return true;
                case OPEN:
                    if (now - openedAt > openMillis) {
                        state = State.HALF_OPEN;
                        return true;
                    }
                    return false;
                case HALF_OPEN:
                    return true;
            }
            return true;
        }

        void recordSuccess() {
            failures = 0;
            if (state != State.CLOSED) state = State.CLOSED;
        }

        void recordFailure() {
            failures++;
            if (state == State.HALF_OPEN || failures >= failureThreshold) {
                state = State.OPEN;
                openedAt = System.currentTimeMillis();
            }
        }

        State getState() { return state; }
    }

    static class LoadBalancer {
        private final List<ServiceNode> nodes;
        private final Map<ServiceNode, CircuitBreaker> breakers = new HashMap<>();
        private int rr = 0;

        LoadBalancer(List<ServiceNode> nodes) {
            this.nodes = nodes;
            for (ServiceNode n : nodes) {
                breakers.put(n, new CircuitBreaker(2, 1000));
            }
        }

        boolean handle(Request r) {
            int attempts = 0;
            int maxAttempts = Math.max(nodes.size() + 2, nodes.size() * 2);
            long baseBackoffMs = 5;
            while (attempts < maxAttempts) {
                ServiceNode n = pick();
                CircuitBreaker cb = breakers.get(n);
                if (!cb.allow()) { attempts++; continue; }
                boolean ok = n.handle(r);
                if (ok) {
                    cb.recordSuccess();
                    return true;
                } else {
                    cb.recordFailure();
                    long exp = Math.min(100, (long)(baseBackoffMs * Math.pow(2.0, attempts)));
                    double jitter = ThreadLocalRandom.current().nextDouble(0.5, 1.5);
                    long sleepMs = Math.max(1, (long)(exp * jitter));
                    try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
                    attempts++;
                }
            }
            return false;
        }

        private ServiceNode pick() {
            int size = nodes.size();
            // Pass 1: Prefer CLOSED and healthy nodes
            for (int i = 0; i < size; i++) {
                ServiceNode n = nodes.get((rr + i) % size);
                CircuitBreaker cb = breakers.get(n);
                if (cb.getState() == CircuitBreaker.State.CLOSED && n.healthy) {
                    rr = (rr + i + 1) % size;
                    return n;
                }
            }
            // Pass 2: Any CLOSED nodes
            for (int i = 0; i < size; i++) {
                ServiceNode n = nodes.get((rr + i) % size);
                CircuitBreaker cb = breakers.get(n);
                if (cb.getState() == CircuitBreaker.State.CLOSED) {
                    rr = (rr + i + 1) % size;
                    return n;
                }
            }
            // Pass 3: HALF_OPEN nodes
            for (int i = 0; i < size; i++) {
                ServiceNode n = nodes.get((rr + i) % size);
                CircuitBreaker cb = breakers.get(n);
                if (cb.getState() == CircuitBreaker.State.HALF_OPEN) {
                    rr = (rr + i + 1) % size;
                    return n;
                }
            }
            // Fallback: any node (even OPEN) to avoid starvation
            ServiceNode fallback = nodes.get(rr % size);
            rr = (rr + 1) % size;
            return fallback;
        }
    }

    static class ChaosMonkey {
        private final List<ServiceNode> nodes;
        ChaosMonkey(List<ServiceNode> nodes) { this.nodes = nodes; }

        void introduceChaos() {
            ServiceNode target = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            boolean flip = ThreadLocalRandom.current().nextBoolean();
            if (flip) {
                target.healthy = false;
                target.failureRate = 0.2;
                System.out.println("[CHAOS] Degraded " + target.name);
            } else {
                target.healthy = true;
                target.failureRate = Math.max(0.01, target.failureRate / 2);
                System.out.println("[CHAOS] Healed " + target.name);
            }
        }
    }

    static class Metrics {
        private final AtomicLong ok = new AtomicLong();
        private final AtomicLong fail = new AtomicLong();
        private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        void record(boolean success, long latencyMs) {
            if (success) ok.incrementAndGet(); else fail.incrementAndGet();
            latencies.add(latencyMs);
        }

        void print() {
            long total = ok.get() + fail.get();
            Collections.sort(latencies);
            long p50 = percentile(50);
            long p95 = percentile(95);
            long p99 = percentile(99);
            System.out.println("\n=== Simulation Results ===");
            System.out.printf("Requests: %d, Success: %d, Fail: %d, SuccessRate: %.2f%%%n",
                    total, ok.get(), fail.get(), (ok.get() * 100.0) / total);
            System.out.printf("Latency p50=%dms p95=%dms p99=%dms%n", p50, p95, p99);
        }

        private long percentile(int p) {
            if (latencies.isEmpty()) return 0;
            int idx = Math.min(latencies.size()-1, (int)Math.ceil((p/100.0)*latencies.size())-1);
            return latencies.get(idx);
        }
    }
} 