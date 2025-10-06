## Madrasati Reliability Simulation
The **Madrasati Reliability Simulation** is a simple Java application designed to simulate a service with multiple nodes behind a load balancer. It incorporates various reliability mechanisms such as **circuit breakers**, **load balancing**, **retries with jittered exponential backoff**, and a **chaos monkey**. The simulation prints success rates and latency percentiles (p50, p95, p99) at the end.


### Features
- **Load balancing**: prefers healthier nodes.
- **Circuit breaker**: trips quickly and recovers after a cooldown.
- **Retries**: multiple attempts per request with jittered exponential backoff.
- **Chaos monkey**: periodically degrades or heals nodes to simulate failures.
- **Metrics**: success/failure count and latency percentiles (p50, p95, p99).

### Project layout
- `Main.java`: entry point in package `edu.madrasati.sim`.

### Prerequisites
- Java 17+ (any modern JDK should work)

### Build
Compile to the `out` directory:

```bash
javac -d out Main.java
```

### Run
Run the main class using the compiled output:

```bash
java -cp out edu.madrasati.sim.Main
```

#### CLI options
- `--nodes=<int>`: number of service nodes (default: 5)
- `--requests=<int>`: number of requests to simulate (default: 10000)
- `--failure=<double>`: base per-node failure rate in [0,1] (default: 0.01)
- `--chaosEvery=<int>`: introduce a chaos event every N requests (default: 300)

Examples:

```bash
java -cp out edu.madrasati.sim.Main --nodes=8 --requests=20000 --failure=0.02 --chaosEvery=200
```

### Example output

```text
Madrasati Reliability Simulation
[CHAOS] ...

=== Simulation Results ===
Requests: 10000, Success: 10000, Fail: 0, SuccessRate: 100.00%
Latency p50=3ms p95=6ms p99=17ms
```

### How it works (high level)
- `ServiceNode`: simulates latency and probabilistic failures; can be degraded/healed.
- `CircuitBreaker`: transitions between CLOSED/OPEN/HALF_OPEN to avoid repeated failures.
- `LoadBalancer`: routes requests to nodes, favoring healthy nodes and honoring circuit states; retries with jittered backoff on failures.
- `ChaosMonkey`: occasionally toggles node health/failure rate.
- `Metrics`: tracks outcomes and computes percentile latencies.

## Humanization Checklist Application

| Feature | Status |
|---|---|
| Teacher Video Introductions | Not Applied |
| Arabic Interface & Islamic Values | Applied |
| Mobile-First Responsive Design | Applied |
| Screen Reader Compatibility | Not Applied |
| Intuitive, Clean UI | Not Applied |
| Voice/Video Feedback on Assignments | Not Applied |
| Student Welcome Survey | Not Applied |
| Discussion Forums & Social Learning | Applied |
| Gamification: Tracking & Achievements | Not Applied |
| Real-Time Support & Chat | Applied |
| Robust Search & Filtering | Applied |
| Offline Content Access | Applied |
| Multiple Assessment Formats | Applied |
| Data Analytics & Progress Tracking | Applied |
| Security & Privacy Compliance | Applied |
