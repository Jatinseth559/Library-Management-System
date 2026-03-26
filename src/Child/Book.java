package Child;
import Parent.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Book extends Item
{
    private final String DB_URL = "jdbc:mysql://localhost:3306/LMS";
    private final String USER = "root";
    private final String PASS = "";

    public Book() {
        loadFromDatabase();
    }

    // Database connection
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public boolean bookExists(int bookId) {
        Node temp = head;
        while (temp != null) {
            if (temp.id == bookId) return true;
            temp = temp.next;
        }
        return false;
    }
    public void addBookSorted(int bookId) {

        Scanner sc = new Scanner(System.in);
        while (bookExists(bookId)) {
            try {
                System.out.print("⚠️ Book with this ID already exists! Please enter a different ID: ");
                bookId = sc.nextInt();
                sc.nextLine(); // clear buffer
                if (bookId <= 0) throw new IllegalArgumentException("ID must be greater than 0.");
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter a numeric value for ID.");
                sc.nextLine(); // clear invalid input
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }

        String title = "";
        String author = "";
        double rentAmount = 0;
        int quantity = 0;

        // Take Title
        while (true) {
            System.out.print("Enter Title: ");
            title = sc.nextLine().trim();
            if (!title.isEmpty()) break;
            System.out.println("⚠️ Title cannot be empty. Please try again.");
        }

        // Take Author (only alphabets + spaces)
        while (true) {
            System.out.print("Enter Author: ");
            author = sc.nextLine().trim();
            if (!author.isEmpty() && author.chars().allMatch(c -> Character.isLetter(c) || Character.isWhitespace(c))) {
                break;
            }
            System.out.println("⚠️ Author name must contain only alphabets and spaces.");
        }

        // Rent Amount
        while (true) {
            try {
                System.out.print("Enter Rent Amount: ");
                rentAmount = Double.parseDouble(sc.nextLine().trim());
                if (rentAmount > 0) break;
                System.out.println("⚠️ Rent Amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid input! Enter a numeric value.");
            }
        }

        // Quantity
        while (true) {
            try {
                System.out.print("Enter Quantity: ");
                quantity = Integer.parseInt(sc.nextLine().trim());
                if (quantity > 0) break;
                System.out.println("⚠️ Quantity must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid input! Enter an integer.");
            }
        }

        int type = -1; // Book type

        Node n = new Node(bookId, title, author, rentAmount, quantity, type);

        // Insert into doubly linked list sorted by ID
        if (head == null || bookId < head.id) {
            n.next = head;
            if (head != null) head.prev = n;
            head = n;
        } else {
            Node temp = head;
            while (temp.next != null && temp.next.id < bookId) temp = temp.next;
            n.next = temp.next;
            if (temp.next != null) temp.next.prev = n;
            temp.next = n;
            n.prev = temp;
        }

        // Save into DB
        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement("INSERT INTO books VALUES (?, ?, ?, ?, ?)")) {
            pst.setInt(1, bookId);
            pst.setString(2, title);
            pst.setString(3, author);
            pst.setDouble(4, rentAmount);
            pst.setInt(5, quantity);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }

        // ✅ Push to undo stack **inside Item**
        undoStack.push(new Action("ADD", bookId, title, author, rentAmount, quantity, type,"Book"));
        redoStack.clear();

        System.out.println("✅ Book added successfully!");
    }

    public void deleteBook(int bookId) {
        Node temp = head;
        while (temp != null && temp.id != bookId) {
            temp = temp.next;
        }
        if (temp == null) {
            System.out.println("❌ Book not found.");
            return;
        }

        // Push to Undo Stack (type = -1 for book)
        undoStack.push(new Action("DELETE", temp.id, temp.title, temp.author, temp.price, temp.quantity, -1,"Book"));
        redoStack.clear();

        // Remove from linked list
        if (temp.prev != null) temp.prev.next = temp.next;
        else head = temp.next;
        if (temp.next != null) temp.next.prev = temp.prev;

        // Delete from database (no null initialization, explicit close)
        Connection conn;
        PreparedStatement pst;
        try {
            conn = connect();
            pst = conn.prepareStatement("DELETE FROM books WHERE bookId = ?");
            pst.setInt(1, bookId);
            pst.executeUpdate();

            // ✅ Close explicitly
            pst.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }

        System.out.println("✅ Book deleted.");
    }

    public void displayBooksFromDatabase() {
        String query = "SELECT * FROM books ORDER BY bookId";

        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            // Header
            System.out.printf("\n%-6s %-20s %-20s %-10s %-10s\n",
                    "ID", "Title", "Author", "Rent", "Quantity");
            System.out.println("---------------------------------------------------------------");

            // Rows
            while (rs.next()) {
                int id = rs.getInt("bookId");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("rentAmount"); // Node.price
                int qty = rs.getInt("quantity");

                System.out.printf("%-6d %-20s %-20s ₹%-9.2f %-10s\n",
                        id, title, author, price, (qty == 0 ? "Unavailable" : qty));
            }

        } catch (SQLException e) {
            System.out.println("DB Load Error: " + e.getMessage());
        }
    }


    public void displayBooks() {
        if (head == null) {
            System.out.println("📚 Library is empty.");
            return;
        }

        Node temp = head;
        System.out.println("\n📖 Book List:");

        while (temp != null) {
            String status = (temp.quantity == 0) ? "[Currently Unavailable]" : "[Available: " + temp.quantity + "]";
            System.out.printf("ID: %d | Title: %s | Author: %s | Rent: ₹%.2f %s\n",
                    temp.id, temp.title, temp.author, temp.price, status);
            temp = temp.next;
        }
    }


    public void updateBook(Scanner sc) {
        int bookId;
        Node temp = null;

        while (true) {
            try {
                System.out.print("Enter Book ID to update: ");
                bookId = sc.nextInt();
                sc.nextLine(); // consume newline

                temp = findBookById(bookId);

                if (temp == null) {
                    System.out.println("❌ Book not found. Please try again.");
                    continue; // go back and ask again
                }

                break; // ✅ valid ID found, exit loop
            }
            catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter a numeric Book ID.");
                sc.nextLine(); // clear buffer
            }
        }


        double oldPrice = temp.price;
        int oldQty = temp.quantity;

        int option;
        // Take option safely
        while (true) {
            try {
                System.out.println("\n1. Update Rent Amount\n2. Update Quantity");
                System.out.print("Choose an option: ");
                option = sc.nextInt();
                sc.nextLine(); // consume newline
                if (option != 1 && option != 2) {
                    throw new IllegalArgumentException("Please choose 1 or 2.");
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter a number (1 or 2).");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = connect();

            switch (option) {
                case 1: { // Update Rent
                    double newPrice;
                    while (true) {
                        try {
                            System.out.print("Enter new Rent Amount: ");
                            newPrice = sc.nextDouble();
                            sc.nextLine();
                            if (newPrice <= 0) {
                                throw new IllegalArgumentException("Rent Amount must be greater than 0.");
                            }
                            break;
                        } catch (InputMismatchException e) {
                            System.out.println("⚠️ Invalid input! Please enter a numeric value.");
                            sc.nextLine();
                        } catch (IllegalArgumentException e) {
                            System.out.println("⚠️ " + e.getMessage());
                        }
                    }

                    temp.price = newPrice;
                    pst = conn.prepareStatement("UPDATE books SET rentAmount = ? WHERE bookId = ?");
                    pst.setDouble(1, newPrice);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                    pst.close();

                    // Push to undo stack with type = -1 for Book
                    undoStack.push(new Action(
                            "UPDATE", bookId, temp.title, temp.author,
                            oldPrice, oldQty, newPrice, oldQty,
                            -1,"Book"
                    ));
                    redoStack.clear();

                    System.out.println("✅ Rent updated.");
                    break;
                }

                case 2: { // Update Quantity
                    int newQty;
                    while (true) {
                        try {
                            System.out.print("Enter new Quantity: ");
                            newQty = sc.nextInt();
                            sc.nextLine();
                            if (newQty <= 0) {
                                throw new IllegalArgumentException("Quantity must be greater than 0.");
                            }
                            break;
                        } catch (InputMismatchException e) {
                            System.out.println("⚠️ Invalid input! Please enter an integer value.");
                            sc.nextLine();
                        } catch (IllegalArgumentException e) {
                            System.out.println("⚠️ " + e.getMessage());
                        }
                    }

                    temp.quantity = newQty;
                    pst = conn.prepareStatement("UPDATE books SET quantity = ? WHERE bookId = ?");
                    pst.setInt(1, newQty);
                    pst.setInt(2, bookId);
                    pst.executeUpdate();
                    pst.close();

                    // Push to undo stack with type = -1 for Book
                    undoStack.push(new Action(
                            "UPDATE", bookId, temp.title, temp.author,
                            oldPrice, oldQty, oldPrice, newQty,
                            -1,"Book"
                    ));
                    redoStack.clear();

                    System.out.println("✅ Quantity updated.");
                    break;
                }
            }

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("DB Closing Error: " + e.getMessage());
            }
        }
    }

    public void searchBook(Scanner sc) {
        int choice = 0;

        // Take valid choice safely
        while (true) {
            try {
                System.out.println("\nSearch Book By:");
                System.out.println("1. Title");
                System.out.println("2. Author");
                System.out.print("Enter choice: ");
                choice = sc.nextInt();
                sc.nextLine(); // consume newline

                if (choice != 1 && choice != 2) {
                    throw new IllegalArgumentException("Please choose 1 (Title) or 2 (Author).");
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("⚠️ Invalid input! Please enter a number (1 or 2).");
                sc.nextLine(); // clear buffer
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        }

        String column = (choice == 1) ? "title" : "author";

        // Take valid search value
        String searchValue = "";
        while (true) {
            try {
                System.out.print("Enter " + column + " to search: ");
                searchValue = sc.nextLine().trim();

                if (searchValue.isEmpty()) {
                    throw new IllegalArgumentException(column + " cannot be empty.");
                }

                // Extra validation for Author → only alphabets and spaces
                if (choice == 2) {
                    boolean valid = true;
                    for (char c : searchValue.toCharArray()) {
                        if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                            valid = false;
                            break;
                        }
                    }
                    if (!valid) {
                        throw new IllegalArgumentException("Author name must contain only alphabets and spaces.");
                    }
                }

                break;
            } catch (Exception e) {
                System.out.println("⚠️ " + e.getMessage() + " Please enter again.");
            }
        }

        Connection conn;
        PreparedStatement pst;
        ResultSet rs;

        try {
            conn = connect();
            String query = "SELECT * FROM books WHERE LOWER(" + column + ") LIKE ? ORDER BY bookId";
            pst = conn.prepareStatement(query);
            pst.setString(1, "%" + searchValue.toLowerCase() + "%");
            rs = pst.executeQuery();

            boolean found = false;

            System.out.printf("\n%-6s %-20s %-20s %-10s %-10s\n", "ID", "Title", "Author", "Rent", "Quantity");
            System.out.println("---------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("bookId");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("rentAmount");
                int qty = rs.getInt("quantity");

                int type = -1; // Book type, for internal use only

                System.out.printf("%-6d %-20s %-20s ₹%-9.2f %-10s\n",
                        id, title, author, price, (qty == 0 ? "Unavailable" : qty));
                found = true;
            }

            if (!found) {
                System.out.println("❌ No books found for given " + column + ".");
            }

            // Close resources manually
            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (Search): " + e.getMessage());
        }
    }

    public void rentBook(Scanner sc, String Uname) {
        String searchTitle = "";
        while (true) {
            System.out.print("Enter Book Title to Rent: ");
            searchTitle = sc.nextLine().trim();
            if (!searchTitle.isEmpty()) break;
            System.out.println("⚠️ Title cannot be empty. Please try again.");
        }
        searchTitle = searchTitle.toLowerCase(); // normalize

        Node temp = head;
        while (temp != null) {
            if (temp.title.toLowerCase().equals(searchTitle)) {

                if (temp.quantity == 0) {
                    System.out.println("❌ Book is currently unavailable.");
                    return;
                }

                String renterName = Uname.trim();
                temp.quantity--;

                // --- Update quantity in DB ---
                updateItemQuantityInDB(temp.id, temp.quantity, -1); // -1 for Book

                // --- Full deposit upfront ---
                double depositAmount = temp.price;
                LocalDate rentDate = LocalDate.now();

                // --- Save rental history in DB ---
                Connection conn = null;
                PreparedStatement pst = null;
                try {
                    conn = connect();
                    pst = conn.prepareStatement(
                            "INSERT INTO rental_history " +
                                    "(bookId, title, renterName, rentAmount, rentDate, returnDate, finalAmount, status) " +
                                    "VALUES (?, ?, ?, ?, ?, NULL, NULL, ?)");
                    pst.setInt(1, temp.id);
                    pst.setString(2, temp.title);
                    pst.setString(3, renterName);
                    pst.setDouble(4, depositAmount);
                    pst.setDate(5, java.sql.Date.valueOf(rentDate));
                    pst.setString(6, "RENTED");
                    pst.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("DB Error (Insert Rental Book): " + e.getMessage());
                } finally {
                    try {
                        if (pst != null) pst.close();
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        System.out.println("DB Close Error: " + e.getMessage());
                    }
                }

                // --- Save rental history in File (Append Mode) ---
                FileWriter fw = null;
                BufferedWriter bw = null;
                PrintWriter out = null;
                try {
                    fw = new FileWriter("rental_history.txt", true);
                    bw = new BufferedWriter(fw);
                    out = new PrintWriter(bw);
                    out.printf("%d | %s | %s | ₹%.2f | %s | STATUS: %s%n",
                            temp.id, temp.title, renterName, depositAmount, rentDate, "RENTED");
                } catch (IOException e) {
                    System.out.println("File Error (Insert Rental Book): " + e.getMessage());
                } finally {
                    try {
                        if (out != null) out.close();
                        if (bw != null) bw.close();
                        if (fw != null) fw.close();
                    } catch (IOException e) {
                        System.out.println("File Close Error: " + e.getMessage());
                    }
                }

                // --- Push to undo stack ---
                undoStack.push(new Action(
                        "RENT",
                        temp.id,
                        temp.title,
                        temp.author,
                        temp.price,
                        1,            // rented quantity
                        renterName,
                        -1,           // type = -1 for book
                        "Book"
                ));
                redoStack.clear();

                System.out.printf("✅ Book Rented! Deposit Taken: ₹%.2f | Rent Date: %s\n", depositAmount, rentDate);
                return;
            }
            temp = temp.next;
        }

        System.out.println("❌ Book not found.");
    }


    public void viewRentalHistory() {
        // --- First: Display from DB ---
        String query = "SELECT * FROM rental_history ORDER BY rentDate DESC";
        Connection conn;
        Statement stmt;
        ResultSet rs;

        try {
            conn = connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            System.out.println("\n--- Rental History (DB) ---");
            System.out.printf("%-6s %-6s %-20s %-20s %-25s\n", "ID", "Type", "Title", "Renter", "Date");
            System.out.println("------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("bookId");
                String title = rs.getString("title");
                String renter = rs.getString("renterName");
                Timestamp date = rs.getTimestamp("rentDate");

                System.out.printf("%-6d %-6s %-20s %-20s %-25s\n", id, "Book", title, renter, date);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (View Rental History): " + e.getMessage());
        }

        // --- Second: Display from TXT file ---
        System.out.println("\n--- Rental History (TXT File) ---");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("rental_history.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("File Error (View Rental History): " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }

        // --- Third: Display using Stored Procedure ---
        System.out.println("-------View using Procedure---------");
        String callQuery = "{CALL RentalHistory()}";
        Connection connProc;
        CallableStatement cstmt;
        ResultSet rsProc;

        try {
            connProc = connect();
            cstmt = connProc.prepareCall(callQuery);
            rsProc = cstmt.executeQuery();

            System.out.printf("\n%-6s %-20s %-20s %-25s\n", "ID", "Title", "Renter", "Date");
            System.out.println("--------------------------------------------------------------");

            boolean found = false;
            while (rsProc.next()) {
                int id = rsProc.getInt("bookId");
                String title = rsProc.getString("title");
                String renter = rsProc.getString("renterName");
                Timestamp date = rsProc.getTimestamp("rentDate");

                System.out.printf("%-6d %-20s %-20s %-25s\n", id, title, renter, date);
                found = true;
            }

            if (!found) {
                System.out.println("❌ No rental history found.");
            }

            rsProc.close();
            cstmt.close();
            connProc.close();

        } catch (SQLException e) {
            System.out.println("DB Error (View Rental History Procedure): " + e.getMessage());
        }
    }

    public void viewRentalHistoryUser(String Uname) {
        String query = "SELECT * FROM rental_history WHERE renterName = ? ORDER BY rentDate";
        Connection conn;
        PreparedStatement pst;
        ResultSet rs;

        try {
            conn = connect();
            pst = conn.prepareStatement(query);
            pst.setString(1, Uname); // safely bind renterName
            rs = pst.executeQuery();

            System.out.printf("\n%-6s %-20s %-20s %-25s\n", "ID", "Title", "Renter", "Date");
            System.out.println("--------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                int id = rs.getInt("bookId");
                String title = rs.getString("title");
                String renter = rs.getString("renterName");
                Timestamp date = rs.getTimestamp("rentDate");

                System.out.printf("%-6d %-20s %-20s %-25s\n", id, title, renter, date);
                found = true;
            }

            if (!found) {
                System.out.println("❌ No rental history found for " + Uname);
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (View Rental History User): " + e.getMessage());
        }
    }

    public void returnBook(Scanner sc, String Uname) {
        String title = "";
        while (true) {
            System.out.print("Enter Book Title to Return: ");
            title = sc.nextLine().trim();
            if (!title.isEmpty()) break;
            System.out.println("⚠️ Title cannot be empty. Please try again.");
        }
        title = title.toLowerCase();

        // Step 1: Find book in catalog
        Node temp = head;
        Node foundBook = null;
        while (temp != null) {
            if (temp.title.toLowerCase().equals(title)) {
                foundBook = temp;
                break;
            }
            temp = temp.next;
        }

        if (foundBook == null) {
            System.out.println("❌ Book not found in catalog.");
            return;
        }

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT rentalId, rentAmount, rentDate FROM rental_history " +
                             "WHERE renterName = ? AND LOWER(title) = ? AND returnDate IS NULL " +
                             "ORDER BY rentDate DESC LIMIT 1")) {

            pst.setString(1, Uname);
            pst.setString(2, title);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ You have not rented this book, or it is already returned.");
                return;
            }

            int rentalId = rs.getInt("rentalId");
            double deposit = rs.getDouble("rentAmount");
            LocalDate rentDate = rs.getDate("rentDate").toLocalDate();
            rs.close();

            // Calculate days and rent
            LocalDate returnDate = LocalDate.now();
            long days = ChronoUnit.DAYS.between(rentDate, returnDate);
            double finalAmount;
            String status;

            if (days <= 10) {
                finalAmount = deposit + (days * 0.08);
                status = "RETURNED";
            } else if (days <= 15) {
                finalAmount = deposit + (days * 0.1);
                status = "RETURNED";
            } else {
                finalAmount = deposit; // deposit converted to profit
                status = "SOLD";
                System.out.println("⚠️ Return period exceeded 15 days. Deposit converted to profit.");
            }

            // Update quantity only if within return period
            if (!status.equals("SOLD")) {
                foundBook.quantity++;
                updateItemQuantityInDB(foundBook.id, foundBook.quantity, -1); // -1 for book
            }

            // Update rental history
            try (PreparedStatement pst2 = conn.prepareStatement(
                    "UPDATE rental_history SET returnDate = ?, finalAmount = ?, status = ? WHERE rentalId = ?")) {
                pst2.setDate(1, java.sql.Date.valueOf(returnDate));
                pst2.setDouble(2, finalAmount);
                pst2.setString(3, status);
                pst2.setInt(4, rentalId);
                pst2.executeUpdate();
            }

            // Undo stack
            undoStack.push(new Action(
                    "RETURN",
                    foundBook.id,
                    foundBook.title,
                    foundBook.author,
                    foundBook.price,
                    1,
                    Uname,
                    -1,
                    "Book"
            ));
            redoStack.clear();

            System.out.printf("✅ Book processed. Rent Amount: ₹%.2f | Status: %s | Days: %d\n",
                    finalAmount, status, days);

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }

    public void printMonthlyProfitBook() {
        String query = "SELECT YEAR(rentDate) AS year, MONTH(rentDate) AS month, SUM(finalAmount) AS profit " +
                "FROM rental_history_book " +
                "WHERE finalAmount IS NOT NULL " +   // only completed rentals
                "GROUP BY YEAR(rentDate), MONTH(rentDate) " +
                "ORDER BY YEAR(rentDate), MONTH(rentDate)";

        try (Connection conn = connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            System.out.println("📊 Monthly Profit Report (Books):");
            System.out.println("Year | Month | Profit");

            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double profit = rs.getDouble("profit");
                System.out.printf("%d |   %02d  | ₹%.2f\n", year, month, profit);
            }

        } catch (SQLException e) {
            System.out.println("DB Error (Monthly Profit Books): " + e.getMessage());
        }
    }


    // Load books from database

    public void loadFromDatabase()
    {
        Connection conn;
        Statement stmt;
        ResultSet rs;

        try {
            conn = connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books ORDER BY bookId");

            while (rs.next()) {
                int id = rs.getInt("bookId");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double rent = rs.getDouble("rentAmount");
                int qty = rs.getInt("quantity");

                Node newNode = new Node(id, title, author, rent, qty,-1);

                // Insert in sorted order
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

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("DB Error (loadFromDatabase): " + e.getMessage());
        }
    }


    // Implement other Book methods here (delete, update, undo, redo, etc.)
}

