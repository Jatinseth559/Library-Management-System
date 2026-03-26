-- =========================
-- TABLES
-- =========================

CREATE TABLE User_Credentials (
    User_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    sec_question VARCHAR(100) NOT NULL,
    sec_answer VARCHAR(100) NOT NULL,
);
CREATE TABLE rental_history_magazine (
    rentalId INT AUTO_INCREMENT PRIMARY KEY,
    magazineId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    renterName VARCHAR(100) NOT NULL,
    rentAmount DOUBLE NOT NULL,
    rentDate DATE NOT NULL,
    returnDate DATE,        -- NULL if not yet returned
    finalAmount DOUBLE,     -- Amount to be calculated at return
    status VARCHAR(20),     -- e.g., 'RENTED', 'RETURNED', 'SOLD'
    FOREIGN KEY (magazineId) REFERENCES magazine(magazineId)
);
CREATE TABLE rental_history (
    rentalId INT AUTO_INCREMENT PRIMARY KEY,
    bookId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    renterName VARCHAR(100) NOT NULL,
    rentAmount DOUBLE NOT NULL,
    rentDate DATE NOT NULL,
    returnDate DATE,        -- NULL if not yet returned
    finalAmount DOUBLE,     -- Amount to be calculated at return
    status VARCHAR(20),     -- e.g., 'RENTED', 'RETURNED', 'SOLD'
    FOREIGN KEY (bookId) REFERENCES book(bookId)
);
CREATE TABLE books (
    bookId INT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    rentAmount DOUBLE NOT NULL,
    quantity INT NOT NULL
);
CREATE TABLE magazine (
    magazineId INT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    rentAmount DOUBLE NOT NULL,
    quantity INT NOT NULL,
    type INT NOT NULL   -- 0 = Read-only, 1 = Rentable
);
CREATE TABLE item_log (
    log_cod INT AUTO_INCREMENT PRIMARY KEY,   -- unique log ID
    item_id INT NOT NULL,                     -- ID of the item
    item_type VARCHAR(50) NOT NULL,           -- type/category of item
    operation VARCHAR(50) NOT NULL,           -- operation performed (INSERT, UPDATE, DELETE, etc.)
    log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- when the log was created
);

-- =========================
-- FUNCTIONS / PROCEDURES
-- =========================

CREATE OR REPLACE FUNCTION RentalHistory()
RETURNS TABLE(
    rental_id INT,
    book_id INT,
    user_name TEXT,
    rentDate TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM rental_history
    ORDER BY rentDate DESC;
END;
$$ LANGUAGE plpgsql;