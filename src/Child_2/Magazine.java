package Child_2;
import Parent.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Magazine extends Item {

    protected Node head; // Head of magazine linked list

    private final String DB_URL = "jdbc:mysql://localhost:3306/LMS";
    private final String USER = "root";
    private final String PASS = "";

    // JDBC connection
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public Magazine() {
        loadFromDatabase();
    }

    // Check if magazine exists
    public boolean magazineExists(int mId) {
        Node temp = head;
        while (temp != null) {
            if (temp.id == mId) return true;
            temp = temp.next;
        }
        return false;
    }
    // Add magazine in sorted order
    public void addMagazineSorted(int mId)
    {
        Scanner sc = new Scanner(System.in);
        while (magazineExists(mId)) {
            try {
                System.out.print("⚠️ Magazine with this ID already exists! Please enter a different ID: ");
                mId = sc.nextInt();
                sc.nextLine();
                if (mId <= 0) throw new IllegalArgumentException("ID must be greater than 0.");
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter a numeric value for ID.");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }


        String title = "";
        String publisher = "";
        double rentAmount = 0;
        int quantity = 0;
        int type = -1;

        // --- Title ---
        while (true) {
            try {
                System.out.print("Enter Title: ");
                title = sc.nextLine().trim();
                if (title.isEmpty())
                    throw new IllegalArgumentException("Title cannot be empty.");
                break;
            } catch (Exception e) {
                System.out.println("⚠️ " + e.getMessage() + " Please enter again.");
            }
        }

        // --- Publisher ---
        while (true) {
            try {
                System.out.print("Enter Publisher: ");
                publisher = sc.nextLine().trim();
                if (publisher.isEmpty())
                    throw new IllegalArgumentException("Publisher cannot be empty.");
                boolean valid = true;
                for (char c : publisher.toCharArray()) {
                    if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                        valid = false;
                        break;
                    }
                }
                if (!valid)
                    throw new IllegalArgumentException("Publisher must contain only alphabets and spaces.");
                break;
            } catch (Exception e) {
                System.out.println("⚠️ " + e.getMessage() + " Please enter again.");
            }
        }

        // --- Type ---
        while (true) {
            try {
                System.out.print("Enter Type (0 = Read-only, 1 = Rentable): ");
                type = sc.nextInt();
                sc.nextLine();
                if (type != 0 && type != 1)
                    throw new IllegalArgumentException("Type must be 0 or 1.");
                break;
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter 0 or 1.");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }

        // --- Rent Amount (only if rentable) ---
        if (type == 1) {
            while (true) {
                try {
                    System.out.print("Enter Rent Amount: ");
                    rentAmount = sc.nextDouble();
                    sc.nextLine();
                    if (rentAmount <= 0)
                        throw new IllegalArgumentException("Rent Amount must be greater than 0.");
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("⚠️ Invalid input! Please enter a numeric value for Rent Amount.");
                    sc.nextLine();
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ " + e.getMessage());
                }
            }
        } else {
            rentAmount = 0; // ✅ for read-only magazines
        }

        // --- Quantity ---
        while (true) {
            try {
                System.out.print("Enter Quantity: ");
                quantity = sc.nextInt();
                sc.nextLine();
                if (quantity <= 0)
                    throw new IllegalArgumentException("Quantity must be greater than 0.");
                break;
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter an integer for Quantity.");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }

        // --- Create Node with type directly ---
        Node n = new Node(mId, title, publisher, rentAmount, quantity, type);

        // --- Insert into sorted DLL ---
        if (head == null || mId < head.id) {
            n.next = head;
            if (head != null) head.prev = n;
            head = n;
        } else {
            Node temp = head;
            while (temp.next != null && temp.next.id < mId) temp = temp.next;
            n.next = temp.next;
            if (temp.next != null) temp.next.prev = n;
            temp.next = n;
            n.prev = temp;
        }

        // --- Insert into database ---
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO magazine (magazineid, title, publisher, rentAmount, quantity, type) VALUES (?, ?, ?, ?, ?, ?)")
        ) {
            pst.setInt(1, mId);
            pst.setString(2, title);
            pst.setString(3, publisher);
            pst.setDouble(4, rentAmount);
            pst.setInt(5, quantity);
            pst.setInt(6, type);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return;
        }

        // --- Push to undo stack ---
        undoStack.push(new Action("ADD", mId, title, publisher, rentAmount, quantity, type, "Magazine"));
        redoStack.clear();

        System.out.println("✅ Magazine added successfully.");
    }



    // Delete magazine
    public void deleteMagazine(int mId) {
        Node temp = head;
        while (temp != null && temp.id != mId) {
            temp = temp.next;
        }

        if (temp == null) {
            System.out.println("❌ Magazine not found.");
            return;
        }

        // Push to undo stack BEFORE deletion
        undoStack.push(new Action(
                "DELETE",
                temp.id,
                temp.title,
                temp.author, // publisher stored as 'author' in Action
                temp.price,
                temp.quantity,
                temp.type, // include type if needed
                "Magazine"
        ));
        redoStack.clear(); // clear redo stack on new action

        // Unlink from linked list
        if (temp.prev != null) temp.prev.next = temp.next;
        else head = temp.next;

        if (temp.next != null) temp.next.prev = temp.prev;

        // Delete from DB
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(
                     "DELETE FROM magazine WHERE magazineid = ?")) {
            pst.setInt(1, mId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }

        System.out.println("✅ Magazine deleted successfully.");
    }

    public Node findMagazineById(int mId) {
        Node temp = head;
        while (temp != null) {
            if (temp.id == mId && temp.type != -1) return temp;
            temp = temp.next;
        }
        return null;
    }

    public void updateMagazine(Scanner sc) {
        int magId;

        // 🔹 Keep asking until valid Magazine ID
        while (true) {
            System.out.print("Enter Magazine ID to update: ");
            if (sc.hasNextInt()) {
                magId = sc.nextInt();
                sc.nextLine();
                break;
            } else {
                System.out.println("❌ Invalid input! ID must be a number.");
                sc.nextLine(); // clear invalid input
            }
        }

        Node temp = findMagazineById(magId); // Node class now
        if (temp == null) {
            System.out.println("❌ Magazine not found.");
            return;
        }

        double oldRent = temp.price;
        int oldQty = temp.quantity;
        int oldType = temp.type;

        int option;
        while (true) {
            if (temp.type == 0) {
                System.out.println("\n1. Update Type\n2. Update Quantity");
            } else {
                System.out.println("\n1. Update Type\n2. Update Rent Amount\n3. Update Quantity");
            }
            System.out.print("Choose an option: ");

            if (sc.hasNextInt()) {
                option = sc.nextInt();
                sc.nextLine();

                if ((temp.type == 0 && (option == 1 || option == 2)) ||
                        (temp.type != 0 && (option == 1 || option == 2 || option == 3))) {
                    break;
                } else {
                    System.out.println("❌ Invalid option! Please choose a valid number.");
                }
            } else {
                System.out.println("❌ Invalid input! Must be a number.");
                sc.nextLine();
            }
        }

        double newRent = oldRent;
        int newQty = oldQty;
        int newType = oldType;

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = connect();

            if (option == 1) { // 🔹 Update Type
                while (true) {
                    System.out.print("Enter new Type (0 for Read-Only, 1 for Rentable): ");
                    if (sc.hasNextInt()) {
                        newType = sc.nextInt();
                        sc.nextLine();
                        break;
                    } else {
                        System.out.println("❌ Invalid input! Type must be a number.");
                        sc.nextLine();
                    }
                }

                temp.type = newType;
                pst = conn.prepareStatement("UPDATE magazine SET type = ? WHERE magazineid = ?");
                pst.setInt(1, newType);
                pst.setInt(2, magId);
                pst.executeUpdate();
                pst.close();

                System.out.println("✅ Type updated.");

            } else if (option == 2 && temp.type != 0) { // 🔹 Update Rent
                while (true) {
                    System.out.print("Enter new Rent Amount: ");
                    if (sc.hasNextDouble()) {
                        newRent = sc.nextDouble();
                        sc.nextLine();
                        if (newRent > 0) break;
                        else System.out.println("❌ Rent must be greater than 0.");
                    } else {
                        System.out.println("❌ Invalid input! Rent must be a number.");
                        sc.nextLine();
                    }
                }

                temp.price = newRent;
                pst = conn.prepareStatement("UPDATE magazine SET rentAmount = ? WHERE magazineid = ?");
                pst.setDouble(1, newRent);
                pst.setInt(2, magId);
                pst.executeUpdate();
                pst.close();

                System.out.println("✅ Rent updated.");

            } else { // 🔹 Update Quantity
                while (true) {
                    System.out.print("Enter new Quantity: ");
                    if (sc.hasNextInt()) {
                        newQty = sc.nextInt();
                        sc.nextLine();
                        if (newQty >= 0) break;
                        else System.out.println("❌ Quantity cannot be negative.");
                    } else {
                        System.out.println("❌ Invalid input! Quantity must be a number.");
                        sc.nextLine();
                    }
                }

                temp.quantity = newQty;
                pst = conn.prepareStatement("UPDATE magazine SET quantity = ? WHERE magazineid = ?");
                pst.setInt(1, newQty);
                pst.setInt(2, magId);
                pst.executeUpdate();
                pst.close();

                System.out.println("✅ Quantity updated.");
            }

            // Push action to undo stack
            undoStack.push(new Action("UPDATE", temp.id, temp.title, temp.author,
                    oldRent, oldQty, newRent, newQty, oldType, "Magazine"));
            redoStack.clear();

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }



    public void searchMagazine(Scanner sc) {
        int choice = -1;

        // Validate menu choice
        while (true) {
            try {
                System.out.println("\nSearch Magazine By:");
                System.out.println("1. Title");
                System.out.println("2. Publisher");
                System.out.print("Enter choice: ");
                choice = Integer.parseInt(sc.nextLine().trim());

                if (choice != 1 && choice != 2) {
                    System.out.println("❌ Invalid choice. Please enter 1 or 2.");
                    continue;
                }
                break; // valid choice
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter only a number (1 or 2).");
            }
        }

        String column = (choice == 1) ? "title" : "publisher";

        // Input validation for search value
        String searchValue;
        while (true) {
            System.out.print("Enter " + column + " to search: ");
            searchValue = sc.nextLine().trim();

            if (searchValue.isEmpty()) {
                System.out.println("❌ " + column + " cannot be empty.");
                continue;
            }

            // Block numbers in publisher name
            if (column.equals("publisher") && searchValue.matches(".*\\d.*")) {
                System.out.println("❌ Publisher cannot contain numbers.");
                continue;
            }

            break;
        }

        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement(
                    "SELECT * FROM magazine WHERE LOWER(" + column + ") LIKE ? ORDER BY magazineId");
            pst.setString(1, "%" + searchValue.toLowerCase() + "%");
            ResultSet rs = pst.executeQuery();

            boolean found = false;
            System.out.printf("\n%-10s %-20s %-20s %-10s %-10s\n",
                    "MagID", "Title", "Publisher", "Rent", "Quantity");
            System.out.println("---------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("magazineId");
                String title = rs.getString("title");
                String publisher = rs.getString("publisher");
                double rent = rs.getDouble("rentAmount");
                int qty = rs.getInt("quantity");

                System.out.printf("%-10d %-20s %-20s ₹%-9.2f %-10s\n",
                        id, title, publisher, rent, (qty == 0 ? "Unavailable" : qty));
                found = true;
            }

            if (!found) {
                System.out.println("❌ No magazines found for given " + column + ".");
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (Search): " + e.getMessage());
        }
    }

    public void rentMagazine(Scanner sc, String Uname) {
        String searchTitle = "";
        while (true) {
            System.out.print("Enter Magazine Title to Rent: ");
            searchTitle = sc.nextLine().trim();

            if (searchTitle.isEmpty()) {
                System.out.println("❌ Title cannot be empty. Please try again.");
            } else {
                break; // valid input
            }
        }
        searchTitle = searchTitle.toLowerCase(); // normalize after validation

        Node temp = head;

        while (temp != null) {
            if (temp.title.toLowerCase().equals(searchTitle)) {

                // Only rentable magazines
                if (temp.type != 1) {
                    System.out.println("❌ This magazine is READ-ONLY and cannot be rented.");
                    return;
                }

                if (temp.quantity == 0) {
                    System.out.println("❌ Magazine is currently unavailable.");
                    return;
                }

                String renterName = Uname.trim();
                temp.quantity--;

                // --- Update quantity in DB ---
                try (Connection conn = connect();
                     PreparedStatement pst = conn.prepareStatement(
                             "UPDATE magazine SET quantity = ? WHERE magazineId = ?")) {
                    pst.setInt(1, temp.quantity);
                    pst.setInt(2, temp.id);
                    pst.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("DB Error (Update Quantity): " + e.getMessage());
                }

                // --- Full deposit upfront ---
                double depositAmount = temp.price;
                LocalDate rentDate = LocalDate.now();

                // --- Save rental history with status = RENTED ---
                try (Connection conn = connect();
                     PreparedStatement pst = conn.prepareStatement(
                             "INSERT INTO rental_history_magazine " +
                                     "(magazineId, title, renterName, rentAmount, rentDate, returnDate, finalAmount, status) " +
                                     "VALUES (?, ?, ?, ?, ?, NULL, NULL, ?)")) {
                    pst.setInt(1, temp.id);
                    pst.setString(2, temp.title);
                    pst.setString(3, renterName);
                    pst.setDouble(4, depositAmount);
                    pst.setDate(5, java.sql.Date.valueOf(rentDate));
                    pst.setString(6, "RENTED");   // <-- NEW FIELD
                    pst.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("DB Error (Insert Rental): " + e.getMessage());
                }

                // --- Push to undo stack ---
                undoStack.push(new Action(
                        "RENT",
                        temp.id,
                        temp.title,
                        temp.author,   // publisher stored as 'author'
                        temp.price,
                        1,             // rented quantity
                        renterName,
                        temp.type,
                        "Magazine"
                ));
                redoStack.clear();

                System.out.printf("✅ Magazine Rented! Deposit Taken: ₹%.2f | Rent Date: %s\n", depositAmount, rentDate);
                return;
            }
            temp = temp.next;
        }

        System.out.println("❌ Magazine not found.");
    }

    public void returnMagazine(Scanner sc, String Uname) {
        String searchTitle = "";
        while (true) {
            System.out.print("Enter Magazine Title to Return: ");
            searchTitle = sc.nextLine().trim();
            if (!searchTitle.isEmpty()) break;
            System.out.println("❌ Title cannot be empty. Please try again.");
        }
        searchTitle = searchTitle.toLowerCase();

        Node temp = head;
        Node foundMag = null;
        while (temp != null) {
            if (temp.title.toLowerCase().equals(searchTitle)) {
                foundMag = temp;
                break;
            }
            temp = temp.next;
        }

        if (foundMag == null) {
            System.out.println("❌ Magazine not found in catalog.");
            return;
        }

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT rentalId, rentAmount, rentDate FROM rental_history_magazine " +
                             "WHERE renterName = ? AND LOWER(title) = ? AND returnDate IS NULL " +
                             "ORDER BY rentDate DESC LIMIT 1")) {

            pst.setString(1, Uname);
            pst.setString(2, searchTitle);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ You have not rented this magazine, or it is already returned.");
                return;
            }

            int rentalId = rs.getInt("rentalId");
            double deposit = rs.getDouble("rentAmount");
            LocalDate rentDate = rs.getDate("rentDate").toLocalDate();
            rs.close();

            LocalDate returnDate = LocalDate.now();
            long days = ChronoUnit.DAYS.between(rentDate, returnDate);
            double finalAmount;
            String status;

            if (days <= 10) {
                finalAmount = deposit + (days * 0.07);
                status = "RETURNED";
            } else if (days <= 15) {
                finalAmount = deposit + (days * 0.09);
                status = "RETURNED";
            } else {
                finalAmount = deposit; // full deposit converted to profit
                status = "SOLD";
                System.out.println("⚠️ Return period exceeded 15 days. Deposit converted to profit.");
            }
            // Update magazine quantity only if within return period
            if (!status.equals("SOLD")) {
                foundMag.quantity++;
                try (PreparedStatement pst2 = conn.prepareStatement(
                        "UPDATE magazine SET quantity = ? WHERE magazineId = ?")) {
                    pst2.setInt(1, foundMag.quantity);
                    pst2.setInt(2, foundMag.id);
                    pst2.executeUpdate();
                }
            }

            // Update rental history with returnDate, finalAmount, and status
            try (PreparedStatement pst3 = conn.prepareStatement(
                    "UPDATE rental_history_magazine SET returnDate = ?, finalAmount = ?, status = ? WHERE rentalId = ?")) {
                pst3.setDate(1, java.sql.Date.valueOf(returnDate));
                pst3.setDouble(2, finalAmount);
                pst3.setString(3, status);
                pst3.setInt(4, rentalId);
                pst3.executeUpdate();
            }

            // Undo stack
            undoStack.push(new Action(
                    "RETURN",
                    foundMag.id,
                    foundMag.title,
                    foundMag.author,
                    foundMag.price,
                    1,
                    Uname,
                    1,
                    "Magazine"
            ));
            redoStack.clear();

            System.out.printf("✅ Magazine processed. Rent Amount: ₹%.2f | Status: %s | Days: %d\n",
                    finalAmount, status, days);

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }

    public void printMonthlyTotalProfit() {
        String query = "SELECT year, month, SUM(profit) AS totalProfit FROM (" +
                "SELECT YEAR(rentDate) AS year, MONTH(rentDate) AS month, SUM(finalAmount) AS profit " +
                "FROM rental_history WHERE finalAmount IS NOT NULL GROUP BY YEAR(rentDate), MONTH(rentDate) " +
                "UNION ALL " +
                "SELECT YEAR(rentDate) AS year, MONTH(rentDate) AS month, SUM(finalAmount) AS profit " +
                "FROM rental_history_magazine WHERE finalAmount IS NOT NULL GROUP BY YEAR(rentDate), MONTH(rentDate) " +
                ") AS combined " +
                "GROUP BY year, month " +
                "ORDER BY year, month";

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            System.out.println("📊 Monthly Total Profit (Books + Magazines):");
            System.out.println("Year | Month | Profit");

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double profit = rs.getDouble("totalProfit");
                System.out.printf("%d |   %02d  | ₹%.2f\n", year, month, profit);
            }

        } catch (SQLException e) {
            System.out.println("DB Error (Monthly Total Profit): " + e.getMessage());
        }
    }


    public void printMonthlyMagazineProfit() {
        String query = "SELECT YEAR(rentDate) AS year, MONTH(rentDate) AS month, SUM(finalAmount) AS profit " +
                "FROM rental_history_magazine " +
                "WHERE finalAmount IS NOT NULL " +   // only completed rentals
                "GROUP BY YEAR(rentDate), MONTH(rentDate) " +
                "ORDER BY YEAR(rentDate), MONTH(rentDate)";

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            System.out.println("📊 Monthly Profit Report:");
            System.out.println("Year | Month | Profit");

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double profit = rs.getDouble("profit");
                System.out.printf("%d |   %02d  | ₹%.2f\n", year, month, profit);
            }

        } catch (SQLException e) {
            System.out.println("DB Error (Monthly Profit): " + e.getMessage());
        }
    }


    public void viewMagazineRentalHistory() {
        // --- Display from DB ---
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM rental_history_magazine ORDER BY rentdate DESC");

            System.out.println("\n--- Magazine Rental History (Database) ---");
            System.out.printf("%-10s %-25s %-20s %-15s %-25s\n",
                    "MagID", "Title", "Renter", "Amount(₹)", "Date");
            System.out.println("-------------------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("magazineid");
                String title = rs.getString("title");
                String renter = rs.getString("renterName");
                double amount = rs.getDouble("rentAmount");
                Timestamp date = rs.getTimestamp("rentdate");

                System.out.printf("%-10d %-25s %-20s %-15.2f %-25s\n",
                        id, title, renter, amount, date);
            }

            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (View Magazine Rental History): " + e.getMessage());
        }

        // --- Display from TXT file ---
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("magazine_rental_history.txt"));
            String line;

            System.out.println("\n--- Magazine Rental History (File) ---");
            System.out.printf("%-10s %-25s %-20s %-15s %-25s\n",
                    "MagID", "Title", "Renter", "Amount(₹)", "Date");
            System.out.println("-------------------------------------------------------------------------------");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    System.out.printf("%-10s %-25s %-20s %-15s %-25s\n",
                            parts[0].trim(), parts[2].trim(), parts[3].trim(),
                            parts[4].trim(), parts[5].trim());
                }
            }

        } catch (IOException e) {
            System.out.println("File Error (View Magazine Rental History): " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }
    }

    public void viewMagazineRentalHistoryUser(String Uname) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = connect();
            String query = "SELECT * FROM rental_history_magazine WHERE renterName = ? ORDER BY rentdate";
            pst = conn.prepareStatement(query);
            pst.setString(1, Uname); // safely bind renterName
            rs = pst.executeQuery();

            System.out.printf("\n%-6s %-20s %-20s %-25s %-10s\n", "ID", "Title", "Renter", "Date", "Amount");
            System.out.println("--------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                int id = rs.getInt("magazineid");
                String title = rs.getString("title");
                String renter = rs.getString("renterName");
                Timestamp date = rs.getTimestamp("rentdate");
                double amount = rs.getDouble("rentAmount");

                System.out.printf("%-6d %-20s %-20s %-25s %-10.2f\n", id, title, renter, date, amount);
                found = true;
            }

            if (!found) {
                System.out.println("❌ No magazine rental history found for " + Uname);
            }

            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (View Magazine Rental History User): " + e.getMessage());
        }
    }

    // Load all magazines from DB into linked list
    public void loadFromDatabase() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            head =null;
            conn = connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM magazine ORDER BY magazineId");

            while (rs.next()) {
                int id = rs.getInt("magazineId");
                String title = rs.getString("title");
                String publisher = rs.getString("publisher");
                double rent = rs.getDouble("rentAmount");
                int qty = rs.getInt("quantity");
                int type = rs.getInt("type");

                Node newNode = new Node(id, title, publisher, rent, qty, type);

                // Insert sorted into DLL
                if (head == null || id < head.id) {
                    newNode.next = head;
                    if (head != null) head.prev = newNode;
                    head = newNode;
                } else {
                    Node temp = head;
                    while (temp.next != null && temp.next.id < id) {
                        temp = temp.next;
                    }
                    newNode.next = temp.next;
                    if (temp.next != null) temp.next.prev = newNode;
                    temp.next = newNode;
                    newNode.prev = temp;
                }
            }

            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();

        } catch (SQLException e) {
            System.out.println("DB Load Error: " + e.getMessage());
        }
    }


    public void displayMagazinesFromDatabase() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = connect();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM magazine ORDER BY magazineId");

            System.out.printf("\n%-6s %-20s %-20s %-10s %-12s %-12s\n",
                    "ID", "Title", "Publisher", "Rent", "Quantity", "Type");
            System.out.println("--------------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("magazineId");
                String title = rs.getString("title");
                String publisher = rs.getString("publisher");
                double rent = rs.getDouble("rentAmount");
                int qty = rs.getInt("quantity");
                int type = rs.getInt("type"); // 0 = read-only, 1 = rentable

                String typeStr = (type == 0) ? "Read-only" : "Rentable";

                System.out.printf("%-6d %-20s %-20s ₹%-9.2f %-12s %-12s\n",
                        id, title, publisher, rent, (qty == 0 ? "Unavailable" : qty), typeStr);
            }

            if (rs != null) rs.close();
            if (st != null) st.close();
            if (conn != null) conn.close();

        } catch (SQLException e) {
            System.out.println("DB Load Error: " + e.getMessage());
        }
    }


    // Display magazines
    public void displayMagazines() {
        if (head == null) {
            System.out.println("📚 No magazines available.");
            return;
        }

        Node temp = head;  // Use Node instead of MagNode
        System.out.println("\n📖 Magazine List:");
        while (temp != null) {
            // Only display magazine nodes (type != -1)
            if (temp.type != -1) {
                String status = (temp.quantity == 0) ? "[Unavailable]" : "[Available: " + temp.quantity + "]";
                System.out.printf("ID: %d | Title: %s | Publisher: %s | Rent: ₹%.2f | Type: %s %s\n",
                        temp.id, temp.title, temp.author, temp.price,
                        (temp.type == 0 ? "Read-only" : "Rentable"), status);
            }
            temp = temp.next;
        }
    }

}