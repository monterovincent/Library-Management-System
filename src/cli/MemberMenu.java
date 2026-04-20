package cli;

import database.MemberDAO;
import domain.Member;
import enums.AccountStatus;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * MemberMenu handles all CLI interactions for member
 * management operations.
 *
 * Menu options:
 *   1. Add a new member
 *   2. Remove a member
 *   3. Update a member
 *   4. Display all members
 *   5. Search members by name
 *   6. Suspend a member account
 *   7. Reactivate a member account
 *   0. Back to main menu
 */
public class MemberMenu {

    // Scanner reads user input from the terminal
    private Scanner scanner;
    private MemberDAO memberDAO;
    
    public MemberMenu(Scanner scanner) {
        this.scanner   = scanner;
        this.memberDAO = new MemberDAO();
    }

    
    /**
     * Displays the member menu and handles user choices.
     * Loops until user selects 0 to go back to main menu.
     */
    public void display() {
        int choice = -1;

        while (choice != 0) {
            printMenu();
            choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    addMember();
                    break;
                case 2:
                    removeMember();
                    break;
                case 3:
                    updateMember();
                    break;
                case 4:
                    displayAllMembers();
                    break;
                case 5:
                    searchByName();
                    break;
                case 6:
                    suspendMember();
                    break;
                case 7:
                    reactivateMember();
                    break;
                case 0:
                    System.out.println(
                        "Returning to main menu...");
                    break;
                default:
                    System.out.println(
                        "Invalid option. Please try again.");
            }
        }
    }

    
    /**
     * Prints the member menu options to the terminal.
     */
    private void printMenu() {
        System.out.println("\n=============================");
        System.out.println("     MEMBER MANAGEMENT       ");
        System.out.println("=============================");
        System.out.println(" 1. Add a new member");
        System.out.println(" 2. Remove a member");
        System.out.println(" 3. Update a member");
        System.out.println(" 4. Display all members");
        System.out.println(" 5. Search by name");
        System.out.println(" 6. Suspend member account");
        System.out.println(" 7. Reactivate member account");
        System.out.println(" 0. Back to main menu");
        System.out.println("=============================");
    }


    /**
     * Prompts user for member details and adds to database.
     */
    private void addMember() {
        System.out.println("\n--- Add New Member ---");

        String name    = getStringInput("Enter Name: ");
        String email   = getStringInput("Enter Email: ");
        String phone   = getStringInput("Enter Phone: ");
        String address = getStringInput("Enter Address: ");

        // validate name and email are not empty
        if (name.isEmpty() || email.isEmpty()) {
            System.out.println(
                "Error: Name and Email cannot be empty.");
            return;
        }

        // get borrow limit with a default value
        int limit = getIntInput(
            "Enter max borrow limit (default 5): ");

        // new members always start ACTIVE with today's date
        Member member = new Member(
            0,
            name,
            email,
            phone,
            address,
            AccountStatus.ACTIVE,
            new Date(),
            limit > 0 ? limit : 5
        );

        memberDAO.addMember(member);
    }


    /**
     * Prompts user for member ID and removes from database.
     */
    private void removeMember() {
        System.out.println("\n--- Remove Member ---");

        int memberId = getIntInput("Enter Member ID to remove: ");

        // confirm before deleting
        String confirm = getStringInput(
            "Are you sure you want to remove member ID " +
            memberId + "? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            memberDAO.removeMember(memberId);
        } else {
            System.out.println("Remove cancelled.");
        }
    }

    
    /**
     * Prompts user for updated member details and updates
     * the database record.
     */
    private void updateMember() {
        System.out.println("\n--- Update Member ---");

        int memberId = getIntInput("Enter Member ID to update: ");

        Member existing = memberDAO.getMemberById(memberId);

        if (existing == null) {
            System.out.println(
                "Error: No member found with ID: " + memberId);
            return;
        }

        System.out.println("Current details: "
                + existing.toString());

        
        String name = getStringInput(
            "Enter new Name (" + existing.getName() + "): ");
        String email = getStringInput(
            "Enter new Email (" + existing.getEmail() + "): ");
        String phone = getStringInput(
            "Enter new Phone (" + existing.getPhone() + "): ");
        String address = getStringInput(
            "Enter new Address (" +
            existing.getAddress() + "): ");

        // build updated member preserving unchanged fields
        Member updated = new Member(
            memberId,
            name.isEmpty()    ? existing.getName()    : name,
            email.isEmpty()   ? existing.getEmail()   : email,
            phone.isEmpty()   ? existing.getPhone()   : phone,
            address.isEmpty() ? existing.getAddress() : address,
            existing.getStatus(),
            existing.getMembershipDate(),
            existing.getMaxBorrowLimit()
        );

        memberDAO.updateMember(updated);
    }

    
    /**
     * Retrieves and displays all members from the database.
     */
    private void displayAllMembers() {
        System.out.println("\n--- All Members ---");

        List<Member> members = memberDAO.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (Member member : members) {
            System.out.println(member.toString());
        }
        System.out.println("Total members: " + members.size());
    }

    /**
     * Prompts user for a name and displays matching members.
     * Uses partial matching so for exampel "Smith" finds "John Smith".
     */
    private void searchByName() {
        System.out.println("\n--- Search Members by Name ---");

        String name = getStringInput("Enter name to search: ");

        if (name.isEmpty()) {
            System.out.println("Error: Name cannot be empty.");
            return;
        }

        List<Member> results = memberDAO.searchByName(name);

        if (results.isEmpty()) {
            System.out.println(
                "No members found matching: " + name);
        } else {
            for (Member member : results) {
                System.out.println(member.toString());
            }
            System.out.println(
                "Results found: " + results.size());
        }
    }

    /**
     * Prompts for member ID and sets account to SUSPENDED.
     */
    private void suspendMember() {
        System.out.println("\n--- Suspend Member Account ---");

        int memberId = getIntInput("Enter Member ID: ");

        memberDAO.updateMemberStatus(
            memberId, AccountStatus.SUSPENDED);
    }

    /**
     * Prompts for member ID and sets account back to ACTIVE.
     */
    private void reactivateMember() {
        System.out.println(
            "\n--- Reactivate Member Account ---");

        int memberId = getIntInput("Enter Member ID: ");

        memberDAO.updateMemberStatus(
            memberId, AccountStatus.ACTIVE);
    }


    /**
     * Reads a String input from the user.
     *
     * @param prompt - message displayed to user
     * @return       - String input
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        //Trims whitespace from both ends of input
        return scanner.nextLine().trim();
    }

    /**
     * Reads an integer input from the user.
     * Validates input is a valid integer.
     * Loops until valid integer is entered.
     *
     * @param prompt - message displayed to user
     * @return       - valid integer input
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(
                        scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                // input validation
                System.out.println(
                    "Invalid input. Please enter a number.");
            }
        }
    }
}
