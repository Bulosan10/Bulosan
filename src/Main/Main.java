package Main;

import java.util.*;
import Config.Config;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Config db = new Config();

    public static void main(String[] args) {
        db.connectDB();
        boolean running = true;

        while (running) {
            System.out.println("\n===== WATER BILLING SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    loginUser();
                    break;
                case 2:
                    registerUser();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you! Program Ended.");
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    // ---------------- REGISTER ----------------
    private static void registerUser() {
        System.out.println("\n=== REGISTER USER ===");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        while (!db.fetchRecords("SELECT * FROM tbl_user WHERE u_email = ?", email).isEmpty()) {
            System.out.print("Email already exists, Enter another Email: ");
            email = sc.nextLine();
        }

        System.out.print("Enter Type (1 - Admin / 2 - Client): ");
        int type = getIntInput();
        String userType = (type == 1) ? "Admin" : "Client";
        String status = (userType.equals("Admin")) ? "Approved" : "Pending";

        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        db.addRecord(
            "INSERT INTO tbl_user(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)",
            name, email, userType, status, pass
        );

        if (userType.equals("Admin")) {
            System.out.println("Admin registered and approved successfully!");
        } else {
            System.out.println("Client registered! Waiting for Admin approval.");
        }
    }

    // ---------------- LOGIN ----------------
    private static void loginUser() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        List<Map<String, Object>> result = db.fetchRecords(
            "SELECT * FROM tbl_user WHERE u_email = ? AND u_pass = ?", email, pass
        );

        if (result.isEmpty()) {
            System.out.println("INVALID CREDENTIALS");
            return;
        }

        Map<String, Object> user = result.get(0);
        String status = user.get("u_status").toString();
        String type = user.get("u_type").toString();

        if (status.equalsIgnoreCase("Pending")) {
            System.out.println("Account is Pending. Contact the Admin!");
            return;
        } else if (status.equalsIgnoreCase("Disapproved")) {
            System.out.println("Account is Disapproved. Access denied.");
            return;
        }

        System.out.println("LOGIN SUCCESS!");

        if (type.equalsIgnoreCase("Admin")) {
            adminDashboard();
        } else if (type.equalsIgnoreCase("Client")) {
            clientDashboard(Integer.parseInt(user.get("u_id").toString()));
        } else {
            System.out.println("Unknown user type.");
        }
    }

    // ---------------- ADMIN DASHBOARD ----------------
    private static void adminDashboard() {
        boolean stay = true;

        while (stay) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. View Users");
            System.out.println("2. Approve/Disapprove Clients");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. View Client Profiles");
            System.out.println("6. View Meter Readings");
            System.out.println("7. View and Generate Bills");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    viewUsers();
                    break;
                case 2:
                    approveClients();
                    break;
                case 3:
                    updateUserFlow();
                    break;
                case 4:
                    deleteUserFlow();
                    break;
                case 5:
                    viewAllProfiles();
                    break;
                case 6:
                    viewAllMeterReadings();
                    break;
                case 7:
                    viewAllBills();
                    break;
                case 0:
                    System.out.println("Logging out...");
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    // ---------------- CLIENT DASHBOARD ----------------
    private static void clientDashboard(int userId) {
        boolean stay = true;

        while (stay) {
            System.out.println("\n=== CLIENT DASHBOARD ===");
            System.out.println("1. View Profile");
            System.out.println("2. Add/Edit Profile");
            System.out.println("3. Add Meter Reading");
            System.out.println("4. Generate Bill");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    viewClientProfile(userId);
                    break;
                case 2:
                    addOrEditProfile(userId);
                    break;
                case 3:
                    addMeterReading(userId);
                    break;
                case 4:
                    generateBill(userId);
                    break;
                case 0:
                    System.out.println("Logging out...");
                    stay = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    // ---------------- CLIENT FUNCTIONS ----------------
    private static void viewClientProfile(int userId) {
        List<Map<String, Object>> data = db.fetchRecords("SELECT * FROM tbl_profile WHERE user_id = ?", userId);
        if (data.isEmpty()) {
            System.out.println("No profile found. Please add your profile first.");
            return;
        }

        Map<String, Object> p = data.get(0);
        System.out.println("\n--- Client Profile ---");
        System.out.println("Status: " + p.get("status"));
        System.out.println("Contact: " + p.get("contact"));
        System.out.println("Gmail: " + p.get("gmail"));
        System.out.println("Age: " + p.get("age"));
        System.out.println("Gender: " + p.get("gender"));
    }

    private static void addOrEditProfile(int userId) {
        System.out.print("Enter Status ");
        String status = sc.nextLine();
        System.out.print("Enter Contact Number: ");
        String contact = sc.nextLine();
        System.out.print("Enter Gmail: ");
        String gmail = sc.nextLine();
        System.out.print("Enter Age: ");
        int age = getIntInput();
        System.out.print("Enter Gender: ");
        String gender = sc.nextLine();

        List<Map<String, Object>> exists = db.fetchRecords("SELECT * FROM tbl_profile WHERE user_id = ?", userId);
        if (exists.isEmpty()) {
            db.addRecord("INSERT INTO tbl_profile(user_id, status, contact, gmail, age, gender) VALUES (?, ?, ?, ?, ?, ?)",
                    userId, status, contact, gmail, age, gender);
            System.out.println("Profile added successfully!");
        } else {
            db.updateRecord("UPDATE tbl_profile SET status=?, contact=?, gmail=?, age=?, gender=? WHERE user_id=?",
                    status, contact, gmail, age, gender, userId);
            System.out.println("Profile updated successfully!");
        }
    }

    private static void addMeterReading(int userId) {
        System.out.print("Enter current meter reading (in cubic meters): ");
        double reading = Double.parseDouble(sc.nextLine());
        System.out.print("Enter reading date (YYYY-MM-DD HH:MM:SS): ");
        String date = sc.nextLine();

        db.addRecord("INSERT INTO tbl_meter(user_id, reading, date) VALUES (?, ?, ?)", userId, reading, date);
        System.out.println("Meter reading added!");
    }

    private static void generateBill(int userId) {
        List<Map<String, Object>> readings = db.fetchRecords(
                "SELECT * FROM tbl_meter WHERE user_id = ? ORDER BY date DESC LIMIT 2", userId);

        if (readings.size() < 2) {
            System.out.println("Need at least 2 readings to generate bill.");
            return;
        }

        double latest = Double.parseDouble(readings.get(0).get("reading").toString());
        double previous = Double.parseDouble(readings.get(1).get("reading").toString());
        double consumption = latest - previous;
        double rate = 25.0;
        double total = consumption * rate;

        System.out.println("\n--- Water Bill ---");
        System.out.println("Previous Reading: " + previous);
        System.out.println("Current Reading: " + latest);
        System.out.println("Consumption: " + consumption + " cu.m");
        System.out.println("Rate: ₱" + rate);
        System.out.println("Total Bill: ₱" + total);
        System.out.print("Enter bill date (YYYY-MM-DD HH:MM:SS): ");
        String date = sc.nextLine();

        db.addRecord("INSERT INTO tbl_bill(user_id, previous_reading, current_reading, consumption, total, date) VALUES (?, ?, ?, ?, ?, ?)",
                userId, previous, latest, consumption, total, date);
        System.out.println("Bill generated successfully!");
    }

    // ---------------- ADMIN FUNCTIONS ----------------
    private static void viewUsers() {
        db.viewRecords("SELECT * FROM tbl_user",
                new String[]{"ID", "Name", "Email", "Type", "Status"},
                new String[]{"u_id", "u_name", "u_email", "u_type", "u_status"});
    }

    private static void approveClients() {
        viewUsers();
        System.out.print("Enter ID to change status (0 to cancel): ");
        int id = getIntInput();
        if (id != 0) {
            System.out.println("1. Approved");
            System.out.println("2. Disapproved");
            System.out.print("Enter choice: ");
            int statusChoice = getIntInput();
            String newStatus = (statusChoice == 1) ? "Approved" : "Disapproved";
            db.updateRecord("UPDATE tbl_user SET u_status = ? WHERE u_id = ?", newStatus, id);
            System.out.println("User status updated to: " + newStatus);
        }
    }

    private static void updateUserFlow() {
        viewUsers();
        System.out.print("Enter ID to update (0 to cancel): ");
        int idUpdate = getIntInput();
        if (idUpdate != 0) {
            System.out.print("Enter new Name: ");
            String name = sc.nextLine();
            System.out.print("Enter new Type (1 - Admin / 2 - Client): ");
            int type = getIntInput();
            String tp = (type == 1) ? "Admin" : "Client";
            db.updateRecord("UPDATE tbl_user SET u_name = ?, u_type = ? WHERE u_id = ?", name, tp, idUpdate);
            System.out.println("User updated!");
        }
    }

    private static void deleteUserFlow() {
        viewUsers();
        System.out.print("Enter ID to delete (0 to cancel): ");
        int idDelete = getIntInput();
        if (idDelete != 0) {
            db.updateRecord("DELETE FROM tbl_user WHERE u_id = ?", idDelete);
            System.out.println("User deleted!");
        }
    }

    private static void viewAllProfiles() {
        db.viewRecords("SELECT * FROM tbl_profile",
                new String[]{"Profile ID", "User ID", "Status", "Contact", "Gmail", "Age", "Gender"},
                new String[]{"profile_id", "user_id", "status", "contact", "gmail", "age", "gender"});
    }

    private static void viewAllMeterReadings() {
        db.viewRecords("SELECT * FROM tbl_meter",
                new String[]{"Meter ID", "User ID", "Reading", "Date"},
                new String[]{"meter_id", "user_id", "reading", "date"});
    }

    private static void viewAllBills() {
        db.viewRecords("SELECT * FROM tbl_bill",
                new String[]{"Bill ID", "User ID", "Prev", "Curr", "Consumption", "Total", "Date"},
                new String[]{"bill_id", "user_id", "previous_reading", "current_reading", "consumption", "total", "date"});
    }

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}

