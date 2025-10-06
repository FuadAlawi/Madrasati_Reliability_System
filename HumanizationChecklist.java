
import java.util.*;
public class HumanizationChecklist {

    static class ChecklistItem {
        String description;
        boolean isApplied;

        ChecklistItem(String description) {
            this.description = description;
            this.isApplied = false;
        }

        void apply(boolean status) {
            isApplied = status;
        }

        @Override
        public String toString() {
            return description + (isApplied ? " [✅ Applied]" : " [❌ Not Applied]");
        }
    }

    private List<ChecklistItem> checklistItems = new ArrayList<>();

    public HumanizationChecklist() {
        String[] principles = {
            "Low-bandwidth optimization", "WCAG 2.1 AA compliance", "Multi-language support (Arabic, English, French)",
            "Device compatibility across platforms", "Islamic calendar integration", "Right-to-left (RTL) text support",
            "Cultural color schemes", "Prayer time considerations", "AES-256 encryption at rest", "TLS 1.3 for data in transit",
            "Role-based access controls", "Comprehensive audit logging"
        };
        for (String principle : principles) {
            checklistItems.add(new ChecklistItem(principle));
        }
    }

    public void askUserToApplyPrinciples() {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < checklistItems.size(); i++) {
            ChecklistItem item = checklistItems.get(i);
            System.out.println("Apply principle (T/F): " + item.description);
            String input = scanner.nextLine().toUpperCase();
            if (input.equals("T")) item.apply(true);
            else if (input.equals("F")) item.apply(false);
            else {
                System.out.println("Invalid input, enter 'T' or 'F'.");
                i--;
            }
        }
    }
    public void displayChecklist() {
        System.out.println("\nHumanization Checklist for Madrasati System:\n");
        checklistItems.forEach(System.out::println);
    }
    public static void main(String[] args) {
        HumanizationChecklist checklist = new HumanizationChecklist();
        checklist.askUserToApplyPrinciples();
        checklist.displayChecklist();
    }
}