package Parent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Item {
    private final String DB_URL = "jdbc:mysql://localhost:3306/LMS";
    private final String USER = "root";
    private final String PASS = "";

    // Common Node class for items (Books, Magazines, etc.)
    protected class Node {
        public int id;         // Item ID
        public String title;
       public  String author;   // author for Book, publisher for Magazine
        public double price;    // Rent amount or issue price
       public  int quantity;    // Stock count
       public  int type;        // -1 = Book, 0 = ReadOnly Magazine, 1 = Rentable Magazine
       public  Node prev, next; // Doubly Linked List pointers

       public Node(int id, String title, String author, double price, int quantity, int type) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.price = price;
            this.quantity = quantity;
            this.type = type;
        }
    }

    // HistoryStack class to manage Undo/Redo operations
    public static class HistoryStack
    {
        private final Action[] stack;
        private int top;
        private final int maxSize;

        public HistoryStack(int size) {
            this.stack = new Action[size];
            this.top = -1;
            this.maxSize = size;
        }

        public void push(Action action) {
            if (top < maxSize - 1) {
                stack[++top] = action;
            } else {
                System.out.println("⚠️ Stack is full, cannot push action: " + action);
            }
        }

        public Action pop() {
            if (top >= 0) {
                return stack[top--];
            }
            return null;
        }

        // ✅ Peek at the top element without removing it
        public Action peek() {
            if (top >= 0) {
                return stack[top];
            }
            return null;
        }

        public void clear() {
            top = -1;
        }

        public boolean isEmpty() {
            return top == -1;
        }

        // ✅ Optional: get current size
        public int size() {
            return top + 1;
        }
    }


    // ✅ Action class with type2 (human-readable type reference, not DB)
    public class Action {
         String actionType;  // "ADD", "DELETE", "UPDATE", "RENT", "RETURN"
         int id;
         String title;
         String author;       // Book author or Magazine publisher
        double oldPrice, newPrice;
        int oldQty, newQty;
        String renterName;   // only used for RENT/RETURN
        int type;            // -1 for Book, 0/1 for Magazine
        public String type2;        // "Book", "Magazine", etc. (not stored in DB)

        // Constructor for RENT or RETURN actions
        public Action(String actionType, int id, String title, String author,
                      double price, int qty, String renterName, int type, String type2) {
            this.actionType = actionType;
            this.id = id;
            this.title = title;
            this.author = author;
            this.oldPrice = price;
            this.newPrice = price;
            this.oldQty = qty;
            this.newQty = qty;
            this.renterName = renterName;
            this.type = type;
            this.type2 = type2;
        }

        // Constructor for UPDATE actions
        public Action(String actionType, int id, String title, String author,
                      double oldPrice, int oldQty, double newPrice, int newQty, int type, String type2) {
            this.actionType = actionType;
            this.id = id;
            this.title = title;
            this.author = author;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
            this.oldQty = oldQty;
            this.newQty = newQty;
            this.type = type;
            this.type2 = type2;
        }

        // Constructor for ADD or DELETE actions
        public Action(String actionType, int id, String title, String author,
                      double price, int qty, int type, String type2) {
            this.actionType = actionType;
            this.id = id;
            this.title = title;
            this.author = author;
            this.oldPrice = price;
            this.newPrice = price;
            this.oldQty = qty;
            this.newQty = qty;
            this.type = type;
            this.type2 = type2;
            this.renterName = ""; // not applicable
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // ==================== UNDO ====================
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("❌ Nothing to undo.");
            return;
        }

        Action last = undoStack.pop();
        redoStack.push(last);

        switch (last.actionType) {
            case "ADD":
                deleteItemNoHistory(last.id, last.type);
                System.out.println("↩️ Undo: Deleted " + last.type2 + " removed.");
                break;

            case "DELETE":
                addItemNoHistory(last.id, last.title, last.author, last.oldPrice, last.oldQty, last.type);
                System.out.println("↩️ Undo: Deleted " + last.type2 + " restored.");
                break;

            case "RENT": {
                Node n = (last.type == -1) ? findBookById(last.id) : findMagazineById(last.id);
                if (n != null) {
                    n.quantity++;
                    updateItemQuantityInDB(n.id, n.quantity, last.type);
                    deleteItemRentalHistory(n.id, last.renterName, last.type);
                }
                System.out.println("↩️ Undo: " + last.type2 + " rent reverted.");
                break;
            }

            case "RETURN": {
                Node n = (last.type == -1) ? findBookById(last.id) : findMagazineById(last.id);
                if (n != null && n.quantity > 0) {
                    n.quantity--;
                    updateItemQuantityInDB(n.id, n.quantity, last.type);
                }
                System.out.println("↩️ Undo: " + last.type2 + " return reverted.");
                break;
            }

            case "UPDATE":
                applyItemUpdateNoHistory(last.id, last.oldPrice, last.oldQty, last.type);
                System.out.println("↩️ Undo: " + last.type2 + " update reverted.");
                break;

            default:
                System.out.println("❌ Unknown action type: " + last.actionType);
        }
    }

    // ==================== REDO ====================
    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("❌ Nothing to redo.");
            return;
        }

        Action act = redoStack.pop();
        undoStack.push(act);

        switch (act.actionType) {
            case "ADD":
                addItemNoHistory(act.id, act.title, act.author, act.oldPrice, act.oldQty, act.type);
                System.out.println("↪️ Redo: " + act.type2 + " add applied again.");
                break;

            case "DELETE":
                deleteItemNoHistory(act.id, act.type);
                System.out.println("↪️ Redo: " + act.type2 + " delete applied again.");
                break;

            case "RENT": {
                Node n = (act.type == -1) ? findBookById(act.id) : findMagazineById(act.id);
                if (n != null && n.quantity > 0) {
                    n.quantity--;
                    updateItemQuantityInDB(n.id, n.quantity, act.type);
                }
                System.out.println("↪️ Redo: " + act.type2 + " rent applied again.");
                break;
            }

            case "RETURN": {
                Node n = (act.type == -1) ? findBookById(act.id) : findMagazineById(act.id);
                if (n != null) {
                    n.quantity++;
                    updateItemQuantityInDB(n.id, n.quantity, act.type);
                }
                System.out.println("↪️ Redo: " + act.type2 + " return applied again.");
                break;
            }

            case "UPDATE":
                applyItemUpdateNoHistory(act.id, act.newPrice, act.newQty, act.type);
                System.out.println("↪️ Redo: " + act.type2 + " update applied again.");
                break;

            default:
                System.out.println("❌ Unknown action type: " + act.actionType);
        }
    }

    // ==================== Helper Methods ====================
   public Node findBookById(int bookId) {
        Node temp = head;
        while (temp != null) {
            if (temp.id == bookId && temp.type == -1) return temp;
            temp = temp.next;
        }
        return null;
    }

  public Node findMagazineById(int mId) {
        Node temp = head;
        while (temp != null) {
            if (temp.id == mId && temp.type != -1) return temp;
            temp = temp.next;
        }
        return null;
    }

    private void applyItemUpdateNoHistory(int id, double price, int quantity, int type) {
        Node n = (type == -1) ? findBookById(id) : findMagazineById(id);
        if (n != null) {
            n.price = price;
            n.quantity = quantity;
            if (type != -1) n.type = type; // update magazine type in-memory
        }

        try (Connection conn = connect()) {
            PreparedStatement pst;
            if (type == -1) {
                pst = conn.prepareStatement("UPDATE books SET rentAmount = ?, quantity = ? WHERE bookId = ?");
                pst.setDouble(1, price);
                pst.setInt(2, quantity);
                pst.setInt(3, id);
            } else {
                pst = conn.prepareStatement("UPDATE magazine SET rentAmount = ?, quantity = ?, type = ? WHERE magazineId = ?");
                pst.setDouble(1, price);
                pst.setInt(2, quantity);
                pst.setInt(3, type);
                pst.setInt(4, id);
            }
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            System.out.println("DB Error (applyUpdateNoHistory): " + e.getMessage());
        }
    }

    private void deleteItemNoHistory(int id, int type) {
        System.out.println("🟡 Trying to delete ID=" + id + " from " + (type == -1 ? "books" : "magazine"));

        Node temp = head;
        while (temp != null && temp.id != id) temp = temp.next;

        if (temp == null) {
            System.out.println("⚠️ Node with ID=" + id + " not found in memory, but still trying DB delete...");
        } else {
            if (temp.prev != null) temp.prev.next = temp.next;
            else head = temp.next;

            if (temp.next != null) temp.next.prev = temp.prev;
        }

        try (Connection conn = connect()) {
            PreparedStatement pst = (type == -1)
                    ? conn.prepareStatement("DELETE FROM books WHERE bookId = ?")
                    : conn.prepareStatement("DELETE FROM magazine WHERE magazineId = ?");
            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            System.out.println(rows > 0 ? "✅ DB: Deleted item from table." : "❌ DB: No matching row found.");
            pst.close();
        } catch (SQLException e) {
            System.out.println("DB Error (deleteItemNoHistory): " + e.getMessage());
        }
    }
    private void deleteItemRentalHistory(int id, String renterName, int type) {
        String query = (type == -1)
                ? "DELETE FROM rental_history WHERE rentalId = (SELECT rentalId FROM (SELECT rentalId FROM rental_history WHERE bookId = ? AND renterName = ? ORDER BY rentDate DESC LIMIT 1) AS subquery)"
                : "DELETE FROM rental_history_magazine WHERE rentalId = (SELECT rentalId FROM (SELECT rentalId FROM rental_history_magazine WHERE magazineId = ? AND renterName = ? ORDER BY rentDate DESC LIMIT 1) AS subquery)";
        try (Connection conn = connect()) {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            pst.setString(2, renterName);
            int rows = pst.executeUpdate();
            System.out.println(rows > 0 ? "↩️ Last rental history deleted successfully." : "❌ No rental history found to delete.");
            pst.close();
        } catch (SQLException e) {
            System.out.println("DB Error (deleteItemRentalHistory): " + e.getMessage());
        }
    }

    private void addItemNoHistory(int id, String title, String authorOrPublisher, double price, int quantity, int type) {
        Node n = new Node(id, title, authorOrPublisher, price, quantity, type);
        if (head == null || id < head.id) {
            n.next = head;
            if (head != null) head.prev = n;
            head = n;
        } else {
            Node t = head;
            while (t.next != null && t.next.id < id) t = t.next;
            n.next = t.next;
            if (t.next != null) t.next.prev = n;
            t.next = n;
            n.prev = t;
        }

        try (Connection conn = connect()) {
            PreparedStatement pst;
            if (type == -1) {
                pst = conn.prepareStatement("INSERT INTO books (bookId, title, author, rentAmount, quantity) VALUES (?, ?, ?, ?, ?)");
                pst.setInt(1, id);
                pst.setString(2, title);
                pst.setString(3, authorOrPublisher);
                pst.setDouble(4, price);
                pst.setInt(5, quantity);
            } else {
                pst = conn.prepareStatement("INSERT INTO magazine (magazineId, title, publisher, rentAmount, quantity, type) VALUES (?, ?, ?, ?, ?, ?)");
                pst.setInt(1, id);
                pst.setString(2, title);
                pst.setString(3, authorOrPublisher);
                pst.setDouble(4, price);
                pst.setInt(5, quantity);
                pst.setInt(6, type);
            }
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            System.out.println("DB Error (addItemNoHistory): " + e.getMessage());
        }
    }

   public void updateItemQuantityInDB(int id, int quantity, int type) {
        try (Connection conn = connect()) {
            PreparedStatement stmt = (type == -1)
                    ? conn.prepareStatement("UPDATE books SET quantity = ? WHERE bookId = ?")
                    : conn.prepareStatement("UPDATE magazine SET quantity = ? WHERE magazineId = ?");
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(type == -1
                    ? "DB Error (updateQuantityInDB - book): " + e.getMessage()
                    : "DB Error (updateQuantityInDB - magazine): " + e.getMessage());
        }
    }

   public abstract void loadFromDatabase();

    // Protected stack instances for undo/redo
    public static HistoryStack undoStack = new HistoryStack(100);
    public static HistoryStack redoStack = new HistoryStack(100);
    protected Node head = null;
}


