import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    private List<ChecklistItem> checklistItems;

    public HumanizationChecklist() {
        checklistItems = new ArrayList<>();
        initializeChecklist();
    }
    private void initializeChecklist() {
        checklistItems.add(new ChecklistItem("Teacher Video Introductions"));
        checklistItems.add(new ChecklistItem("Arabic Interface & Islamic Values"));
        checklistItems.add(new ChecklistItem("Mobile-First Responsive Design"));
        checklistItems.add(new ChecklistItem("Screen Reader Compatibility"));
        checklistItems.add(new ChecklistItem("Intuitive, Clean UI"));
        checklistItems.add(new ChecklistItem("Voice/Video Feedback on Assignments"));
        checklistItems.add(new ChecklistItem("Student Welcome Survey"));
        checklistItems.add(new ChecklistItem("Discussion Forums & Social Learning"));
        checklistItems.add(new ChecklistItem("Gamification: Tracking & Achievements"));
        checklistItems.add(new ChecklistItem("Real-Time Support & Chat"));
        checklistItems.add(new ChecklistItem("Robust Search & Filtering"));
        checklistItems.add(new ChecklistItem("Offline Content Access"));
        checklistItems.add(new ChecklistItem("Multiple Assessment Formats"));
        checklistItems.add(new ChecklistItem("Data Analytics & Progress Tracking"));
        checklistItems.add(new ChecklistItem("Security & Privacy Compliance"));
    }
    public void askUserToApplyPrinciples() {
        Scanner scanner = new Scanner(System.in);

        for (int i = 0; i < checklistItems.size(); i++) {
            ChecklistItem item = checklistItems.get(i);
            System.out.println("Apply principle? (T/F): " + item.description);
            String userInput = scanner.nextLine().trim().toUpperCase();

            if (userInput.equals("T")) {
                item.apply(true);
            } else if (userInput.equals("F")) {
                item.apply(false);
            } else {
                System.out.println("Invalid input, enter 'T' or 'F'.");
                i--;
            }    } }
    public void displayChecklist() {
        System.out.println("\nHumanization Checklist for Madrasati System:\n");
        checklistItems.forEach(System.out::println);   }
    public static void main(String[] args) {
        HumanizationChecklist checklist = new HumanizationChecklist();

        checklist.askUserToApplyPrinciples();
        checklist.displayChecklist();
    }}
