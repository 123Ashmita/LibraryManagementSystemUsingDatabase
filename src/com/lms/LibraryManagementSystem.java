package com.lms;

import java.sql.*;
import java.util.Scanner;

public class LibraryManagementSystem {

    public static void main(String[] args) {
    	DBConnection.initializeDatabase(); 
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("------ Library Management System ------");
            System.out.println("1. Add Book");
            System.out.println("2. Add Patron");
            System.out.println("3. List Books");
            System.out.println("4. List Patrons");
            System.out.println("5. Borrow Books");
            System.out.println("6. Return Books");           
            System.out.println("7. List Books borrowed by patrons");
            System.out.println("8. Delete Patron");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Book ID: ");
                    int bookId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Title: ");
                    String title = sc.nextLine();
                    if (title.trim().isEmpty()) {
                        System.out.println("Title cannot be empty or just spaces!");
                        break;
                    }
                    System.out.print("Enter Author: ");
                    String author = sc.nextLine();
                    if (author.trim().isEmpty()) {
                        System.out.println("Author cannot be empty or just spaces!");
                        break;
                    }
                    addBook(bookId, title, author);
                    break;

                case 2:
                    System.out.print("Enter Patron ID: ");
                    int patronId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Patron Name: ");
                    String name = sc.nextLine();
                    if (name.trim().isEmpty()) {
                        System.out.println("Name cannot be empty or just spaces!");
                        break;
                    }
                    addPatron(patronId, name);
                    break;

                case 3:
                    listBooks();
                    break;

                case 4:
                	listPatrons();
                    break;
                    
                case 5:
                	System.out.print("Enter Patron ID: ");
                    int borrowPatronId = sc.nextInt();
                    System.out.print("Enter Book ID: ");
                    int borrowBookId = sc.nextInt();
                    borrowBook(borrowPatronId, borrowBookId);
                    break;

                case 6:
                	System.out.print("Enter Patron ID: ");
                    int returnPatronId = sc.nextInt();
                    System.out.print("Enter Book ID: ");
                    int returnBookId = sc.nextInt();
                    returnBook(returnPatronId, returnBookId);
                    break;
                    
                case 7:
                	System.out.println("Enter patron id:");
                	int pid=sc.nextInt();
                	listBorrowedBooksByPatron(pid);
                	break;
                	
               case 8:
            	    System.out.print("Enter Patron ID to delete: ");
            	    int deletePatronId = sc.nextInt();
            	    deletePatron(deletePatronId);
            	    break;

                case 9:
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void addBook(int id, String title, String author) {
        String sql = "INSERT INTO books VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, title.trim());
            ps.setString(3, author.trim());
            ps.executeUpdate();
            System.out.println("Book added!!!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void addPatron(int id, String name) {
        String sql = "INSERT INTO patrons (patron_id, name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

        	ps.setInt(1, id);
        	ps.setString(2, name.trim());
        	ps.executeUpdate();
            System.out.println("Patron added!!!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void listBooks() {
        String sql = "select * from books";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("---- Book List ----");
            while (rs.next()) {
                System.out.println(rs.getInt("book_id") + " | " +
                                   rs.getString("title") + " | " +
                                   rs.getString("author") + " | " +
                                   (rs.getBoolean("is_available") ? "Available" : "Not Available"));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void listPatrons() {
       String sql="select * from patrons";
       try(Connection conn=DBConnection.getConnection();
    		   Statement st=conn.createStatement();
    		   ResultSet rs=st.executeQuery(sql)) {
    	   
    	   while(rs.next()) {
    		      System.out.println(rs.getInt("patron_id") + " | " +
    	                             rs.getString("name"));
    	   }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void borrowBook(int patronId, int bookId) {
        String checkBook = "SELECT is_available FROM books WHERE book_id = ?";
        String borrow = "INSERT INTO borrowed_books (patron_id, book_id) VALUES (?, ?)";
        String updateBook = "UPDATE books SET is_available = FALSE WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkBook)) {

        	checkPs.setInt(1, bookId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getBoolean("is_available")) {
                try (PreparedStatement borrowPs = conn.prepareStatement(borrow);
                     PreparedStatement updatePs = conn.prepareStatement(updateBook)) {

                	borrowPs.setInt(1, patronId);
                	borrowPs.setInt(2, bookId);
                	borrowPs.executeUpdate();

                	updatePs.setInt(1, bookId);
                	updatePs.executeUpdate();

                    System.out.println("Book borrowed successfully.");
                }
            } else {
                System.out.println("Book is not available.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void returnBook(int patronId, int bookId) {
        String delete = "delete from borrowed_books where patron_id = ? and book_id = ?";
        String updateBook = "update books set is_available = true where book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement deletePs = conn.prepareStatement(delete);
             PreparedStatement updatePs = conn.prepareStatement(updateBook)) {

        	deletePs.setInt(1, patronId);
        	deletePs.setInt(2, bookId);
            int rows = deletePs.executeUpdate();

            if (rows > 0) {
            	updatePs.setInt(1, bookId);
            	updatePs.executeUpdate();
                System.out.println("Book returned successfully.");
            } else {
                System.out.println("No such borrow record found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public static void listBorrowedBooksByPatron(int patronId) {
        String sql = "SELECT b.book_id, b.title, b.author " +
                     "FROM books b " +
                     "JOIN borrowed_books bb ON b.book_id = bb.book_id " +
                     "WHERE bb.patron_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

        	ps.setInt(1, patronId);
            ResultSet rs = ps.executeQuery();

            System.out.println("---- Books Borrowed by Patron ID: " + patronId + " ----");
            boolean hasBooks = false;
            while (rs.next()) {
                hasBooks = true;
                System.out.println(rs.getInt("book_id") + " | " +
                                   rs.getString("title") + " | " +
                                   rs.getString("author"));
            }

            if (!hasBooks) {
                System.out.println("No books currently borrowed by this patron.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public static void deletePatron(int patronId) {
        String checkBorrowed = "selct * from borrowed_books WHERE patron_id = ?";
        String deletePatron = "delete from patrons WHERE patron_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkBorrowed)) {

        	checkPs.setInt(1, patronId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                System.out.println("Cannot delete patron — they have borrowed books.");
            } else {
                try (PreparedStatement deletePs = conn.prepareStatement(deletePatron)) {
                    deletePs.setInt(1, patronId);
                    int rowsAffected = deletePs.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Patron deleted successfully.");
                    } else {
                        System.out.println("No such patron found.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}
