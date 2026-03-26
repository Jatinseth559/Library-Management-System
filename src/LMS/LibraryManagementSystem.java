package LMS;
import Child.*;
import Child_2.*;
import Parent.Item;
import java.util.InputMismatchException;
import java.util.Scanner;

public class LibraryManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Book library = new Book();
        Magazine library1 = new Magazine();

        while (true)
        {   // 🔁 Loop back to role selection
            System.out.print("Are you Librarian or Reader? (L/R) or E to Exit: ");
            String role = sc.nextLine().trim().toLowerCase();

            if (role.equals("e") || role.equals("exit")) {
                System.out.println("👋 Exiting system. Goodbye!");
                break;
            }

            if (role.equals("l") || role.equals("librarian")) {
                // Password check with 3 attempts
                final String pass = "admin";
                int attempts = 3;
                boolean success = false;

                while (attempts > 0) {
                    System.out.print("Enter Password: ");
                    String password = sc.nextLine();
                    if (password.equals(pass)) {
                        success = true;
                        break;
                    } else {
                        attempts--;
                        if (attempts > 0) {
                            System.out.println("❌ Wrong password! Attempts remaining: " + attempts);
                        } else {
                            System.out.println("❌ Maximum attempts reached. Returning to role selection...");
                        }
                    }
                }

                if (!success) {
                    continue; // 🔁 go back to role selection
                }

                // ✅ Librarian menu
                boolean librarianFlag = true;
                while (librarianFlag) {
                    System.out.println("\n1. Add \n2. Delete \n3. Update\n4. Search \n5. View Profit \n6. Display \n7. Display (DB)\n8. View Rental History\n9. Undo\n10. Redo\n11. Logout");
                    int choicee;

                    // ✅ Keep asking until valid input
                    while (true) {
                        try {
                            System.out.print("\n📌 Enter your choice: ");
                            choicee = sc.nextInt();
                            sc.nextLine(); // consume newline

                            if (choicee >= 1 && choicee <= 11) {  // ✅ only allow valid menu options
                                break; // valid input → exit loop
                            } else {
                                System.out.println("❌ Invalid choice. Please enter a number between 1 and 11.\n");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("❌ Invalid input! Please enter a number between 1 and 11.\n");
                            sc.nextLine(); // clear wrong input
                        }
                    }
                    switch (choicee) {
                        case 1: // Add
                            int ch;

                            while (true) {
                                try {
                                    System.out.println("\n1. Add Book\n2. Add Magazine");
                                    System.out.print("Enter choice: ");
                                    ch = sc.nextInt();
                                    sc.nextLine();
                                    if (ch == 1 || ch == 2) break;
                                    else System.out.println("❌ Invalid choice. Please select 1 or 2.");
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input! Please enter a number (1 or 2).");
                                    sc.nextLine();
                                }
                            }

                            int id;
                            while (true) {
                                try {
                                    System.out.print(ch == 1 ? "Enter Book ID: " : "Enter Magazine ID: ");
                                    id = sc.nextInt();
                                    sc.nextLine();
                                    break;
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid ID! Please enter a number.");
                                    sc.nextLine();
                                }
                            }

                            if (ch == 1) {
                                library.addBookSorted(id);       // unified Item class handles undo stack internally
                            } else {
                                library1.addMagazineSorted(id);
                            }
                            break;
                        case 2: // Delete
                            int delChoice;
                            while (true) {
                                try {
                                    System.out.println("1. Delete Book");
                                    System.out.println("2. Delete Magazine");
                                    System.out.print("Choose option: ");

                                    delChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (delChoice == 1 || delChoice == 2) {
                                        break; // ✅ valid input, exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1 or 2.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1 or 2).\n");
                                    sc.nextLine(); // clear wrong input
                                }
                            }

                            switch (delChoice) {
                                case 1:
                                    int delBookId;
                                    while (true) {
                                        try {
                                            System.out.print("Enter Book ID to Delete: ");
                                            delBookId = sc.nextInt();
                                            sc.nextLine();
                                            break; // valid input
                                        } catch (InputMismatchException e) {
                                            System.out.println("❌ Invalid input. Please enter a valid Book ID (number).");
                                            sc.nextLine();
                                        }
                                    }
                                    library.deleteBook(delBookId);
                                    break;

                                case 2:
                                    int delMagId;
                                    while (true) {
                                        try {
                                            System.out.print("Enter Magazine ID to Delete: ");
                                            delMagId = sc.nextInt();
                                            sc.nextLine();
                                            break; // valid input
                                        } catch (InputMismatchException e) {
                                            System.out.println("❌ Invalid input. Please enter a valid Magazine ID (number).");
                                            sc.nextLine();
                                        }
                                    }
                                    library1.deleteMagazine(delMagId);
                                    break;
                            }
                            break;
                        case 3: // Update
                            int updateChoice;

                            // ✅ Loop until valid input (1 or 2)
                            while (true) {
                                try {
                                    System.out.println("\nUpdate Menu:");
                                    System.out.println("1. Book");
                                    System.out.println("2. Magazine");
                                    System.out.print("Choose item type to update: ");

                                    updateChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (updateChoice == 1 || updateChoice == 2) {
                                        break; // valid → exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1 or 2.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1 or 2).\n");
                                    sc.nextLine(); // clear wrong input
                                }
                            }

                            // ✅ Now safely call the update functions
                            switch (updateChoice) {
                                case 1:
                                    library.updateBook(sc);
                                    break;
                                case 2:
                                    library1.updateMagazine(sc);
                                    break;
                            }

                            break;
                        case 4: // Search
                            int searchChoice;

                            // ✅ Loop until valid input (1 or 2)
                            while (true) {
                                try {
                                    System.out.println("Search Options:");
                                    System.out.println("1. Book");
                                    System.out.println("2. Magazine");
                                    System.out.print("Enter choice: ");

                                    searchChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (searchChoice == 1 || searchChoice == 2) {
                                        break; // ✅ valid → exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1 or 2.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1 or 2).\n");
                                    sc.nextLine(); // clear invalid input
                                }
                            }

                            // ✅ Safe execution
                            switch (searchChoice) {
                                case 1:
                                    library.searchBook(sc);
                                    break;
                                case 2:
                                    library1.searchMagazine(sc);
                                    break;
                            }

                            break;
                        case 5:
                            boolean inLibraryMenu = true;
                            while (inLibraryMenu) {
                                System.out.println("\n--- Library Menu ---");
                                System.out.println("1. Print Monthly Profit (Magazines)");
                                System.out.println("2. Print Monthly Profit (Books)");
                                System.out.println("3. Print Total Profit (All)");
                                System.out.println("4. Back to Main Menu");
                                System.out.print("Enter your choice: ");

                                int choice = sc.nextInt();
                                sc.nextLine(); // consume newline

                                switch (choice) {
                                    case 1:
                                        library1.printMonthlyMagazineProfit();  // Magazines
                                        break;
                                    case 2:
                                        library.printMonthlyProfitBook(); // Books
                                        break;
                                    case 3:
                                        library1.printMonthlyTotalProfit(); // Combined
                                        break;
                                    case 4:
                                        System.out.println("Returning to main menu...");
                                        inLibraryMenu = false; // exit submenu loop
                                        break;
                                    default:
                                        System.out.println("❌ Invalid choice. Please try again.");
                                }
                            }
                            break;
                        case 6:
                            int dispChoice;

                            // ✅ Loop until valid input (1, 2, or 3)
                            while (true) {
                                try {
                                    System.out.println("\n--- Display Options ---");
                                    System.out.println("1. Display Books");
                                    System.out.println("2. Display Magazines");
                                    System.out.println("3. Display Both");
                                    System.out.print("Enter your choice: ");

                                    dispChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (dispChoice >= 1 && dispChoice <= 3) {
                                        break; // ✅ valid → exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1, 2, or 3).\n");
                                    sc.nextLine(); // clear invalid input
                                }
                            }

                            // ✅ Safe execution
                            switch (dispChoice) {
                                case 1:
                                    library.displayBooks();
                                    break;
                                case 2:
                                    library1.displayMagazines();
                                    break;
                                case 3:
                                    System.out.println("\n--- 📚 Books ---");
                                    library.displayBooks();
                                    System.out.println("\n--- 📖 Magazines ---");
                                    library1.displayMagazines();
                                    break;
                            }

                            break;
                        case 7:
                            int subChoice;

                            // ✅ Keep asking until correct input
                            while (true) {
                                try {
                                    System.out.println("\n📖 What do you want to display?");
                                    System.out.println("1. Books");
                                    System.out.println("2. Magazines");
                                    System.out.println("3. Both");
                                    System.out.print("Enter choice: ");

                                    subChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (subChoice >= 1 && subChoice <= 3) {
                                        break; // ✅ valid → exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1, 2, or 3).\n");
                                    sc.nextLine(); // clear invalid input
                                }
                            }

                            // ✅ Execute based on valid choice
                            switch (subChoice) {
                                case 1:
                                    library.displayBooksFromDatabase();
                                    break;
                                case 2:
                                    library1.displayMagazinesFromDatabase();
                                    break;
                                case 3:
                                    System.out.println("\n--- 📚 Books ---");
                                    library.displayBooksFromDatabase();
                                    System.out.println("\n--- 📖 Magazines ---");
                                    library1.displayMagazinesFromDatabase();
                                    break;
                            }

                            break;
                        case 8:
                            int histChoice;

                            // ✅ Keep asking until correct input
                            while (true) {
                                try {
                                    System.out.println("1. View Book Rental History");
                                    System.out.println("2. View Magazine Rental History");
                                    System.out.print("Enter choice: ");

                                    histChoice = sc.nextInt();
                                    sc.nextLine(); // consume newline

                                    if (histChoice == 1 || histChoice == 2) {
                                        break; // ✅ valid → exit loop
                                    } else {
                                        System.out.println("❌ Invalid choice. Please enter 1 or 2.\n");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("❌ Invalid input. Please enter a number (1 or 2).\n");
                                    sc.nextLine(); // clear invalid input
                                }
                            }

                            // ✅ Execute correct option
                            switch (histChoice) {
                                case 1:
                                    library.viewRentalHistory();
                                    break;
                                case 2:
                                    library1.viewMagazineRentalHistory();
                                    break;
                            }

                            break;
                        case 9:// In Library class, assuming you have an Item object
                            if (!Item.undoStack.isEmpty()) {   // since stacks are in Item class
                                Item.Action last = Item.undoStack.peek(); // check last action

                                if (last.type2.equals("Book")) {
                                    library.undo();   // undo in Book library
                                } else if (last.type2.equals("Magazine")) {
                                    library1.undo();  // undo in Magazine library
                                }
                            } else {
                                System.out.println("⚠️ Nothing to undo.");
                            }
                            break;
                        case 10:
                            if (!Item.redoStack.isEmpty()) {   // since stacks are in Item class
                                Item.Action next = Item.redoStack.peek(); // check next redo action

                                if (next.type2.equals("Book")) {
                                    library.redo();    // redo in Book library
                                } else if (next.type2.equals("Magazine")) {
                                    library1.redo();   // redo in Magazine library
                                }
                            } else {
                                System.out.println("⚠️ Nothing to redo.");
                            }
                            break;
                        case 11:
                            System.out.println("👋 Logging out...");
                            librarianFlag = false;
                            break;
                        default:
                            System.out.println("❌ Invalid choice.");
                    }
                }

            } else if (role.equals("r") || role.equals("reader")) {
                boolean readerMenu = true;
                while (readerMenu) {
                    System.out.println("\n--- Reader Menu ---");
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Enter choice: ");
                    int readerChoice = -1;
                    try {
                        readerChoice = sc.nextInt();
                        sc.nextLine();
                    } catch (InputMismatchException e) {
                        System.out.println("❌ Invalid input. Please enter 1–3.");
                        sc.nextLine();
                        continue;
                    }

                    switch (readerChoice) {
                        case 1: // REGISTER
                            try {
                                String uname = "";
                                while (true) {
                                    try {
                                        System.out.print("Enter your name: ");
                                        uname = sc.nextLine().trim();

                                        // validate empty input
                                        if (uname.isEmpty()) {
                                            System.out.println("⚠️ Name cannot be empty. Please try again.");
                                            continue;
                                        }

                                        // validate only alphabets (no numbers/special chars)
                                        if (!uname.matches("[a-zA-Z ]+")) {
                                            System.out.println("⚠️ Name should contain only letters and spaces. Please try again.");
                                            continue;
                                        }

                                        break; // ✅ valid input, exit loop
                                    }
                                    catch (Exception e) {
                                        System.out.println("❌ Invalid input. Please try again.");
                                        sc.nextLine(); // clear scanner buffer
                                    }
                                }


                                String upass = "";
                                while (true) {
                                    try {
                                        System.out.print("Enter password: ");
                                        upass = sc.nextLine().trim();

                                        // ❌ check empty or only spaces
                                        if (upass.isEmpty()) {
                                            System.out.println("⚠️ Password cannot be empty. Please try again.");
                                            continue;
                                        }

                                        break; // ✅ valid password entered
                                    }
                                    catch (Exception e) {
                                        System.out.println("❌ Invalid input. Please try again.");
                                        sc.nextLine(); // clear scanner buffer
                                    }
                                }

                                String q = "";
                                while (true) {
                                    try {
                                        System.out.print("Enter a security question (e.g., Your pet's name?): ");
                                        q = sc.nextLine().trim();

                                        // ❌ check empty
                                        if (q.isEmpty()) {
                                            System.out.println("⚠️ Security question cannot be empty. Please try again.");
                                            continue;
                                        }

                                        break; // ✅ valid input
                                    }
                                    catch (Exception e) {
                                        System.out.println("❌ Invalid input. Please try again.");
                                        sc.nextLine(); // clear scanner buffer
                                    }
                                }


                                String ans = "";
                                while (true) {
                                    try {
                                        System.out.print("Enter answer to your security question: ");
                                        ans = sc.nextLine().trim();

                                        // ❌ check empty
                                        if (ans.isEmpty()) {
                                            System.out.println("⚠️ Answer cannot be empty. Please try again.");
                                            continue;
                                        }

                                        break; // ✅ valid input
                                    }
                                    catch (Exception e) {
                                        System.out.println("❌ Invalid input. Please try again.");
                                        sc.nextLine(); // clear scanner buffer
                                    }
                                }

                                java.sql.Connection con = java.sql.DriverManager.getConnection(
                                        "jdbc:mysql://localhost:3306/lms", "root", "");
                                java.sql.PreparedStatement pst = con.prepareStatement(
                                        "INSERT INTO User_Credentials (name, password, sec_question, sec_answer) VALUES (?, ?, ?, ?)");
                                pst.setString(1, uname);
                                pst.setString(2, upass);
                                pst.setString(3, q);
                                pst.setString(4, ans);

                                int rows = pst.executeUpdate();
                                if (rows > 0) {
                                    System.out.println("✅ Registered successfully!");
                                }
                                con.close();
                            } catch (Exception e) {
                                System.out.println("❌ Error during registration: " + e.getMessage());
                            }
                            break;

                        case 2: // LOGIN
                            try {
                                String uname = "";
                                while (true) {
                                    try {
                                        System.out.print("Enter your name: ");
                                        uname = sc.nextLine().trim();

                                        // validate empty input
                                        if (uname.isEmpty()) {
                                            System.out.println("⚠️ Name cannot be empty. Please try again.");
                                            continue;
                                        }

                                        // validate only alphabets (no numbers/special chars)
                                        if (!uname.matches("[a-zA-Z ]+")) {
                                            System.out.println("⚠️ Name should contain only letters and spaces. Please try again.");
                                            continue;
                                        }

                                        break; // ✅ valid input, exit loop
                                    }
                                    catch (Exception e) {
                                        System.out.println("❌ Invalid input. Please try again.");
                                        sc.nextLine(); // clear scanner buffer
                                    }
                                }

                                int attempts = 3;
                                boolean loggedIn = false;
                                String dbPassword = "";
                                String securityQ = "", securityAns = "";

                                java.sql.Connection con = java.sql.DriverManager.getConnection(
                                        "jdbc:mysql://localhost:3306/lms", "root", "");
                                java.sql.PreparedStatement pst = con.prepareStatement(
                                        "SELECT password, sec_question, sec_answer FROM User_Credentials WHERE name=? ");
                                pst.setString(1, uname);
                                java.sql.ResultSet rs = pst.executeQuery();

                                if (rs.next()) {
                                    dbPassword = rs.getString("password");
                                    securityQ = rs.getString("sec_question");
                                    securityAns = rs.getString("sec_answer");
                                } else {
                                    System.out.println("❌ No such user found. Please register first.");
                                    con.close();
                                    break;
                                }

                                while (attempts > 0) {
                                    System.out.print("Enter password: ");
                                    String upass = sc.nextLine();
                                    if (upass.equals(dbPassword)) {
                                        System.out.println("✅ Login successful! Welcome " + uname);
                                        loggedIn = true;
                                        break;
                                    } else {
                                        attempts--;
                                        System.out.println("❌ Wrong password. Attempts left: " + attempts);
                                    }
                                }

                                if (!loggedIn) {
                                    System.out.print("Do you want to reset your password? (Yes/No): ");
                                    String resp = sc.nextLine().trim().toLowerCase();
                                    if (resp.equals("yes")) {
                                        System.out.println("Security Question: " + securityQ);
                                        System.out.print("Enter answer: ");
                                        String ans = sc.nextLine().trim();
                                        if (ans.equalsIgnoreCase(securityAns)) {
                                            System.out.print("Enter new password: ");
                                            String newPass = sc.nextLine().trim();

                                            java.sql.PreparedStatement updatePst = con.prepareStatement(
                                                    "UPDATE User_Credentials SET password=? WHERE name=?");
                                            updatePst.setString(1, newPass);
                                            updatePst.setString(2, uname);
                                            updatePst.executeUpdate();

                                            System.out.println("✅ Password updated! Please login again.");
                                        } else {
                                            System.out.println("❌ Incorrect answer. Returning to menu...");
                                        }
                                    }
                                }

                                con.close();

                                // If logged in → open customer flow
                                if (loggedIn) {
                                    boolean customerFlag = true;
                                    while (customerFlag) {
                                        System.out.println("\n1. Rent \n2. Return \n3. Display \n4. Display from DB\n5. Search \n6. View Rental History \n7. Undo \n8. Redo \n9. Logout");
                                        int choice;
                                        while (true) {
                                            try {
                                                System.out.print("Choose an option: ");
                                                choice = sc.nextInt();
                                                sc.nextLine(); // consume newline

                                                if (choice >= 1 && choice <= 9) {
                                                    break; // ✅ valid, exit loop
                                                } else {
                                                    System.out.println("❌ Invalid choice! Please enter a number between 1–9.");
                                                }
                                            } catch (InputMismatchException e) {
                                                System.out.println("❌ Invalid input! Please enter only numbers (1–9).");
                                                sc.nextLine(); // clear bad input
                                            }
                                        }
                                        switch (choice) {
                                            case 1:
                                                int rentChoice;
                                                while (true) {
                                                    try {
                                                        System.out.println("\nChoose Item Type to Rent:");
                                                        System.out.println("1. Book");
                                                        System.out.println("2. Magazine");
                                                        System.out.print("Enter choice: ");
                                                        rentChoice = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (rentChoice == 1 || rentChoice == 2) {
                                                            break; // ✅ valid
                                                        } else {
                                                            System.out.println("❌ Invalid choice. Please enter 1 or 2.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter only numbers (1 or 2).");
                                                        sc.nextLine(); // clear invalid input
                                                    }
                                                }

                                                switch (rentChoice) {
                                                    case 1:
                                                        library.rentBook(sc, uname);
                                                        break;
                                                    case 2:
                                                        library1.rentMagazine(sc, uname);
                                                        break;
                                                }

                                                break;
                                            case 2:
                                                int returnChoice;
                                                while (true) {
                                                    try {
                                                        System.out.println("Return:\n1. Book\n2. Magazine");
                                                        System.out.print("Enter choice: ");
                                                        returnChoice = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (returnChoice == 1 || returnChoice == 2) {
                                                            break; // ✅ valid
                                                        } else {
                                                            System.out.println("❌ Invalid choice. Please enter 1 or 2.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter only numbers (1 or 2).");
                                                        sc.nextLine(); // clear invalid input
                                                    }
                                                }

                                                switch (returnChoice) {
                                                    case 1:
                                                        library.returnBook(sc, uname);
                                                        break;
                                                    case 2:
                                                        library1.returnMagazine(sc, uname);
                                                        break;
                                                }

                                                break;
                                            case 3:
                                                int dispChoice;
                                                while (true) {
                                                    try {
                                                        System.out.println("\n--- Display Options ---");
                                                        System.out.println("1. Display Books");
                                                        System.out.println("2. Display Magazines");
                                                        System.out.println("3. Display Both");
                                                        System.out.print("Enter your choice: ");
                                                        dispChoice = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (dispChoice >= 1 && dispChoice <= 3) {
                                                            break; // ✅ valid choice
                                                        } else {
                                                            System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter numbers only (1–3).");
                                                        sc.nextLine(); // clear wrong input
                                                    }
                                                }

                                                switch (dispChoice) {
                                                    case 1:
                                                        library.displayBooks();
                                                        break;
                                                    case 2:
                                                        library1.displayMagazines();
                                                        break;
                                                    case 3:
                                                        System.out.println("\n--- 📚 Books ---");
                                                        library.displayBooks();
                                                        System.out.println("\n--- 📖 Magazines ---");
                                                        library1.displayMagazines();
                                                        break;
                                                }

                                                break;
                                            case 4:
                                                int subChoice;
                                                while (true) {
                                                    try {
                                                        System.out.println("\n📖 What do you want to display?");
                                                        System.out.println("1. Books");
                                                        System.out.println("2. Magazines");
                                                        System.out.println("3. Both");
                                                        System.out.print("Enter choice: ");
                                                        subChoice = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (subChoice >= 1 && subChoice <= 3) {
                                                            break; // ✅ valid input, exit loop
                                                        } else {
                                                            System.out.println("❌ Invalid choice! Please enter 1, 2, or 3.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter only numbers (1–3).");
                                                        sc.nextLine(); // clear bad input
                                                    }
                                                }

                                                switch (subChoice) {
                                                    case 1:
                                                        library.displayBooksFromDatabase();
                                                        break;
                                                    case 2:
                                                        library1.displayMagazinesFromDatabase();
                                                        break;
                                                    case 3:
                                                        System.out.println("\n--- 📚 Books ---");
                                                        library.displayBooksFromDatabase();
                                                        System.out.println("\n--- 📖 Magazines ---");
                                                        library1.displayMagazinesFromDatabase();
                                                        break;
                                                }

                                                break;
                                            case 5:
                                                int searchChoice;
                                                while (true) {
                                                    try {
                                                        System.out.println("Search Options:");
                                                        System.out.println("1. Book");
                                                        System.out.println("2. Magazine");
                                                        System.out.print("Enter choice: ");
                                                        searchChoice = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (searchChoice == 1 || searchChoice == 2) {
                                                            break; // ✅ valid input
                                                        } else {
                                                            System.out.println("❌ Invalid choice! Please enter 1 or 2.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter only numbers (1 or 2).");
                                                        sc.nextLine(); // clear wrong input
                                                    }
                                                }

                                                switch (searchChoice) {
                                                    case 1:
                                                        library.searchBook(sc);
                                                        break;
                                                    case 2:
                                                        library1.searchMagazine(sc);
                                                        break;
                                                }

                                                break;
                                            case 6:
                                                int subChoice8;
                                                while (true) {
                                                    try {
                                                        System.out.println("1. View Book Rental History");
                                                        System.out.println("2. View Magazine Rental History");
                                                        System.out.print("Choose option: ");
                                                        subChoice8 = sc.nextInt();
                                                        sc.nextLine(); // consume newline

                                                        if (subChoice8 == 1 || subChoice8 == 2) {
                                                            break; // ✅ valid choice
                                                        } else {
                                                            System.out.println("❌ Invalid option! Please enter 1 or 2.");
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println("❌ Invalid input! Please enter only numbers (1 or 2).");
                                                        sc.nextLine(); // clear invalid input
                                                    }
                                                }

                                                switch (subChoice8) {
                                                    case 1:
                                                        library.viewRentalHistoryUser(uname);
                                                        break;
                                                    case 2:
                                                        library1.viewMagazineRentalHistoryUser(uname);
                                                        break;
                                                }

                                                break;
                                            case 7:
                                                // Undo last action
                                                if (!Item.undoStack.isEmpty()) {   // since stacks are in Item class
                                                    Item.Action last = Item.undoStack.peek(); // check last action

                                                    if (last.type2.equals("Book")) {
                                                        library.undo();   // undo in Book library
                                                    } else if (last.type2.equals("Magazine")) {
                                                        library1.undo();  // undo in Magazine library
                                                    }
                                                } else {
                                                    System.out.println("⚠️ Nothing to undo.");
                                                }
                                                break;
                                            case 8:
                                                if (!Item.redoStack.isEmpty()) {   // since stacks are in Item class
                                                    Item.Action next = Item.redoStack.peek(); // check next redo action

                                                    if (next.type2.equals("Book")) {
                                                        library.redo();    // redo in Book library
                                                    } else if (next.type2.equals("Magazine")) {
                                                        library1.redo();   // redo in Magazine library
                                                    }
                                                } else {
                                                    System.out.println("⚠️ Nothing to redo.");
                                                }
                                                break;

                                            case 9:
                                                System.out.println("👋 Logging out...");
                                                customerFlag = false;
                                                break;
                                            default:
                                                System.out.println("❌ Invalid choice.");
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("❌ Error during login: " + e.getMessage());
                            }
                            break;

                        case 3:
                            System.out.println("👋 Exiting Reader menu...");
                            readerMenu = false;
                            break;

                        default:
                            System.out.println("❌ Invalid choice. Please enter 1–3.");
                    }
                }
            } else {
                System.out.println("❌ Invalid input. Please enter 'L', 'R', or 'E'.");
            }
        }
        sc.close();
    }
}