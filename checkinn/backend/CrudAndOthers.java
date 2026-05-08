package backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrudAndOthers {
    private static String URL;
    private static String USER;
    private static String PASS;

    public static boolean createDatabase() throws SQLException {
        Connection conn; // establishing a connection
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createDatabase.");
            return false;
        }

        setConnectionDetails(URL, USER, PASS);
        String sql = "CREATE DATABASE hotelbookingmanagement";
        try (Statement stmt = conn.prepareStatement(sql);) {
            stmt.executeUpdate(sql);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Database already exists, skipping creation.");
            return false;
        }

        // Proceeds here if all goes well
        System.out.println("Database created successfully...");
        return true;
    }

    // --- GETTER METHODS ---
    // --- VARIABLES ---
    public static String getURL() {
        return URL;
    }

    public static String getUSER() {
        return USER;
    }

    public static String getPASS() {
        return PASS;
    }

    // --- GETTER METHODS FOR XML EXPORTING ---
    public static ArrayList<String[]> getAllTableData(String[] cols) throws SQLException {
        Connection conn;
        try {
            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAllBookingData");
            return null;
        }

        // Execute getting all the room details
        ArrayList<String[]> data = new ArrayList<>();

        String query = "SELECT * FROM " + cols[0];
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ArrayList<String> stringArray = new ArrayList<>();

                Pattern idPattern = Pattern.compile("_id");
                Pattern datePattern = Pattern.compile("_date");
                for(int i = 1; i < cols.length; i++){
                    if(cols[i].equals("booking_date") || cols[i].equals("payment_date")){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp date = rs.getTimestamp(cols[i]);
                        stringArray.add(dateFormat.format(date));
                    } else if (cols[i].equals("amount")) { // get double value
                        stringArray.add(Double.toString(rs.getDouble(cols[i])));
                    } else if(idPattern.matcher(cols[i]).find() || cols[i].equals("price") || cols[i].equals("capacity")) {// pattern is an ID type, price, or capacity and thus must be saved as int
                        stringArray.add(Integer.toString(rs.getInt(cols[i])));
                    } else if(datePattern.matcher(cols[i]).find()){ // pattern is date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = rs.getDate(cols[i]);
                        stringArray.add(dateFormat.format(date));
                    } else {
                        stringArray.add(rs.getString(cols[i]));
                    }
                }
                data.add(stringArray.toArray(new String[stringArray.size()]));
            }

        } catch (SQLException e) {
            System.out.println("!Error retrieving table data");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } // Always close the connection
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    // --- OTHER GETTER METHODS ---
    public static Map<LocalDate, Integer> getAvailableRoomsCountForMonth(YearMonth month, Connection conn)
            throws SQLException {
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("USE hotelbookingmanagement");

        // Create a map to store the availability status for each date
        Map<LocalDate, Integer> availabilityMap = new HashMap<>();

        // Define the room types to check
        String[] roomTypes = { "standard", "double", "deluxe", "suite" };

        // Iterate through each day of the month
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            boolean allAvailable = true;
            boolean noneAvailable = true;

            // Check availability for each room type
            for (String roomType : roomTypes) {
                int availableRooms = getAvailableRoomsByType(date.toString(), date.toString(), roomType, conn);
                System.out.println("Available rooms for " + roomType + " on " + date + ": " + availableRooms);

                if (availableRooms > 0) {
                    noneAvailable = false;
                } // At least one room type is available
                else {
                    allAvailable = false;
                } // At least one room type is not available
            }

            // Determine the availability status for the day
            if (allAvailable) {
                availabilityMap.put(date, 1);
            } // All room types are available
            else if (noneAvailable) {
                availabilityMap.put(date, 0);
            } // No room types are available
            else {
                availabilityMap.put(date, 2);
            } // Some room types are available
        }
        return availabilityMap;
    }

    // used on AdminRightPanel to show bookings
    public static ArrayList<String[]> getAllRooms(Date searchDate) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAllRooms");
            return null;
        }

        ArrayList<String[]> rooms = new ArrayList<>();

        String query = "SELECT r.room_number, r.type, b.booking_id, b.booking_date, b.check_in_date, b.check_out_date, r.room_status, b.customer_id "
                +
                "FROM rooms r " +
                "LEFT JOIN bookings b ON r.room_id = b.room_id AND ? BETWEEN b.check_in_date AND b.check_out_date";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, searchDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String[] roomDetails = new String[8];

                    roomDetails[0] = rs.getString("room_number");
                    roomDetails[1] = rs.getString("type");
                    roomDetails[2] = rs.getString("booking_id");

                    Date bookingDate = rs.getDate("booking_date");
                    roomDetails[3] = (bookingDate == null) ? "-" : bookingDate.toString();

                    Date checkIn = rs.getDate("check_in_date");
                    roomDetails[4] = (checkIn == null) ? "-" : checkIn.toString();

                    Date checkOut = rs.getDate("check_out_date");
                    roomDetails[5] = (checkOut == null) ? "-" : checkOut.toString();

                    roomDetails[6] = rs.getString("room_status");
                    roomDetails[7] = rs.getString("customer_id");
                    rooms.add(roomDetails);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAllRooms");
            return null;
        }
        return rooms;
    }

    public static List<Room> getAvailableRooms(String roomType, String checkInDate, String checkOutDate) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            System.out.println("!Error in getAvailableRooms");
            e.printStackTrace();
            return null;
        }

        List<Room> availableRooms = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.type " +
                "FROM rooms r " +
                "WHERE r.type = ? AND r.room_status = 'available' " +
                "AND r.room_id NOT IN (" +
                "  SELECT b.room_id FROM bookings b " +
                "  WHERE b.booking_status = 'confirmed' " +
                "  AND ((" +
                "    (b.check_in_date <= ? AND b.check_out_date > ?) OR " +
                "    (b.check_in_date >= ? AND b.check_in_date < ?) OR " +
                "    (b.check_in_date < ? AND b.check_out_date > ?)" +
                "  ))" +
                ") ";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType.toLowerCase());
            pstmt.setDate(2, java.sql.Date.valueOf(checkInDate));
            pstmt.setDate(3, java.sql.Date.valueOf(checkInDate));
            pstmt.setDate(4, java.sql.Date.valueOf(checkInDate));
            pstmt.setDate(5, java.sql.Date.valueOf(checkOutDate));
            pstmt.setDate(6, java.sql.Date.valueOf(checkOutDate));
            pstmt.setDate(7, java.sql.Date.valueOf(checkInDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Room room = new Room();
                room.roomId = rs.getInt("room_id");
                room.roomNumber = rs.getString("room_number");
                room.type = rs.getString("type");
                room.capacity = getRoomCapacity(rs.getString("type"));
                room.price = (double) getRoomPrice(rs.getString("type"));

                availableRooms.add(room);
            }

        } catch (SQLException e) {
            System.out.println("!Error retrieving available rooms:");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } // Always close the connection
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return availableRooms;
    }

    public static String getAdminFullName(String email) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getFullName");
            return null;
        }

        String sql = "SELECT first_name, last_name FROM staff WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getString("first_name") + " " + rs.getString("last_name");

        } catch (SQLException e) {
            System.out.println("!Error during full name retrieval: " + e.getMessage());
        }
        return null;
    }

    public static int getAvailable(String type, Date dateSelect) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String selectQuery = "SELECT COUNT(r.room_id) AS count " +
                    "FROM rooms r " +
                    "WHERE r.type = ? " +
                    "AND r.room_status = 'available' " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM bookings b " +
                    "    WHERE b.room_id = r.room_id " +
                    "    AND b.booking_status = 'confirmed' " +
                    "    AND ? >= b.check_in_date " +
                    "    AND ? <= b.check_out_date" +
                    ")";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setString(1, type);

                selectStmt.setDate(2, dateSelect);
                selectStmt.setDate(3, dateSelect);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error executing query in getAvailable");
            return -1;
        } finally {
            // Close the connection
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getAvailableRoomsCount(String date, Connection conn) throws SQLException {
        try {
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String query = "SELECT COUNT(r.room_id) AS available_count " +
                    "FROM rooms r " +
                    "WHERE r.room_status = 'available' " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 " +
                    "    FROM bookings b " +
                    "    WHERE b.room_id = r.room_id " +
                    "    AND b.booking_status = 'confirmed' " +
                    "    AND ? >= b.check_in_date " +
                    "    AND ? <= b.check_out_date" +
                    ")";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, date);
                pstmt.setString(2, date);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next())
                        return rs.getInt("available_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAvailableRoomsCount");
            return -1;
        }
        return 0; // Return 0 if no available rooms are found
    }

    // Returns -1 if !Error occurs, expected value if otherwise
    public static int getAvailableRoomsByType(String checkInDate, String checkOutDate, String roomType, Connection conn)
            throws SQLException {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAvailableRoomsByType");
            return -1;
        }
        int availableRooms = 0;
        String query = "SELECT COUNT(*) AS available_count " +
                "FROM rooms r " +
                "WHERE r.type = ? " +
                "AND r.room_status = 'available' " +
                "AND r.room_id NOT IN ( " +
                "    SELECT b.room_id " +
                "    FROM bookings b " +
                "    WHERE b.booking_status = 'confirmed' " +
                "    AND ( ? >= b.check_in_date " +
                "    AND ? <= b.check_out_date ) " +
                ")";
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAvailableRoomsByType");
            return -1;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomType);
            pstmt.setString(2, checkOutDate);
            pstmt.setString(3, checkInDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    availableRooms = rs.getInt("available_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getAvailableRoomsByType");
            return -1;
        }
        return availableRooms;
    }

    public static int getBookId(int customer_id) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getBookId");
            return -1;
        }

        String sql = "SELECT MAX(booking_id) FROM bookings WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customer_id);

            ResultSet rs = pstmt.executeQuery();
            return (rs.next()) ? rs.getInt("MAX(booking_id)") : -1;
        } catch (SQLException e) {
            System.out.println("!Error during booking ID retrieval: " + e.getMessage());
            return -1;
        }
    }

    public static String getBookuuid(int bookID) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getBookId");
            return "-1";
        }

        String sql = "SELECT booking_uuid FROM bookings WHERE booking_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookID);

            ResultSet rs = pstmt.executeQuery();
            return (rs.next()) ? rs.getString("booking_uuid") : "-1";
        } catch (SQLException e) {
            System.out.println("!Error during booking ID retrieval: " + e.getMessage());
            return "-1";
        }
    }

    // customer info for admin right panel dialog box
    public static Customer getCustomerInfo(int customerId) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getCustomerInfo");
            return null;
        }

        String sql = "SELECT customer_id, first_name, last_name, contact_num, email FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("contact_num"),
                        rs.getString("email"));
            } else {
                return null; // Customer not found
            }
        } catch (SQLException e) {
            System.out.println("!Error during customer info retrieval: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("!Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // cstomer class to hold the customer information
    public static class Customer {
        private int customerId;
        private String firstName;
        private String lastName;
        private String contactNum;
        private String email;

        public Customer(int customerId, String firstName, String lastName, String contactNum, String email) {
            this.customerId = customerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactNum = contactNum;
            this.email = email;
        }

        // Getters
        public int getCustomerId() {
            return customerId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getContactNum() {
            return contactNum;
        }

        public String getEmail() {
            return email;
        }

        // Setters
        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setContactNum(String contactNum) {
            this.contactNum = contactNum;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "Customer{" +
                    "customerId=" + customerId +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", contactNum='" + contactNum + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    public static int getCustomerId(String email) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getCustomerId");
            return -1;
        }

        String sql = "SELECT customer_id FROM customers WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return (rs.next()) ? rs.getInt("customer_id") : -1;
        } catch (SQLException e) {
            System.out.println("!Error during customer ID retrieval: " + e.getMessage());
            return -1;
        }
    }

// get booking uuid (to be used on admin right)
    public static String getBookingUUID(int bookingId) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getBookingUUID");
            return null;
        }

        String sql = "SELECT booking_uuid FROM bookings WHERE booking_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("booking_uuid");
            } else {
                return null; // UUID not found
            }

        } catch (SQLException e) {
            System.out.println("!Error in getBookingUUID: " + e.getMessage());
            return null;
        }
    }
    
    public static int getRoomCapacity(String roomType) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getRoomCapacity");
            return -1;
        }

        String sql = "SELECT capacity FROM room_types WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType);

            ResultSet rs = pstmt.executeQuery();
            return (rs.next()) ? rs.getInt("capacity") : -1;
        } catch (SQLException e) {
            System.out.println("!Error during room capacity retrieval: " + e.getMessage());
            return -1;
        }
    }

    public static int getRoomPrice(String roomType) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getRoomPrice");
            return -1;
        }

        int price = 0;
        String query = "SELECT price FROM room_types WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    price = rs.getInt("price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getRoomPrice");
            return -1;
        }

        return price;
    }

    public static int getTotalRooms(String type) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
            String selectQuery = "SELECT COUNT(room_id) AS count FROM rooms WHERE type = ? ";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setString(1, type);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error executing query in getTotal");
            return -1;
        } finally {
            // Close the connection
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Returns -1 if !Error occurs, expected value if otherwise
    public static int getTotalRoomsByType(String roomType) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in getTotalRoomsByType");
            return -1;
        }

        int totalRooms = 0;
        String sql = "SELECT COUNT(*) as total FROM rooms WHERE type = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalRooms = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("!Error in getRoomsByType");
            e.printStackTrace();
            return -1;
        }
        return totalRooms;
    }
    // --- SETTER METHODS ---

    public static void setConnectionDetails(String URL, String USER, String PASS) {
        CrudAndOthers.URL = URL;
        CrudAndOthers.USER = USER;
        CrudAndOthers.PASS = PASS;
    }

    public static void setAllRoomsToAvailable() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String selectQuery = "SELECT room_id FROM ROOMS";
            stmt = conn.createStatement();
            try (ResultSet rooms = stmt.executeQuery(selectQuery);) {
                while (rooms.next()) {
                    int roomID = rooms.getInt("room_id");
                    System.out.println("Updating room_id to available = " + roomID);

                    String updateRoomQuery = "UPDATE rooms SET room_status = 'available' where room_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateRoomQuery);
                    pstmt.setInt(1, roomID);
                    pstmt.executeUpdate();

                    conn.commit();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            System.out.println("!Error executing query in getAvailable");
            return;
        } finally {
            // Close the connection
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setRoomsToUnavailable(Date dateSelect) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
            String selectQuery = "SELECT DISTINCT rooms.room_id FROM rooms INNER JOIN bookings ON rooms.room_id = bookings.room_id WHERE ? >= bookings.check_in_date AND ? <= bookings.check_out_date AND bookings.booking_status = 'confirmed' ";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setDate(1, dateSelect);
                selectStmt.setDate(2, dateSelect);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        int roomID = rs.getInt("room_id");
                        System.out.println("Updating room_id = " + roomID);
                        String updateRoomQuery = "UPDATE rooms SET room_status = 'unavailable' where room_id = ?";
                        PreparedStatement pstmt = conn.prepareStatement(updateRoomQuery);
                        pstmt.setInt(1, roomID);
                        pstmt.executeUpdate();
                    }
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            System.out.println("!Error executing query in getAvailable");
            return;
        } finally {
            // Close the connection
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Get checkout date using booking_id
    public static Date findCheckOutDate(int booking_id) throws SQLException {
        Connection conn = null;
        try {
            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASS);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            // Query to select checkout date
            String selectQuery = "SELECT check_out_date FROM bookings WHERE booking_id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, booking_id);

                // Execute query and return date
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        Date checkOutDate = rs.getDate("check_out_date");
                        System.out.println("Check-out date: " + checkOutDate);
                        return checkOutDate;
                    } else {
                        System.out.println("No booking found with ID: " + booking_id);
                    }
                }
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Closing connection failed: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public static boolean makeRoomAvailable(int bookingid) throws SQLException {
        System.out.println("room_number: " + bookingid);
        // Creating a connection to the database
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in makeRoomAvailable");
            return false;
        }

        // For executing use of hotelbookingmanagement database
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in makeRoomAvailable");
            return false;
        }

        // Execute updating room availability
        ArrayList<String[]> rooms = new ArrayList<String[]>();
        String query = "DELETE FROM bookings WHERE booking_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookingid);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Made room available");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in makeRoomAvailable");
            conn.rollback();
            return false;
        }
        return true;
    }

    // --- CREATE METHODS ---
    public static boolean createCustomers(String lastName, String firstName, String contactNum, String email) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createCustomers");
            return false;
        }

        String sql = "INSERT INTO customers (last_name, first_name, contact_num, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, contactNum);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
            System.out.println("Customer Created Successfully...");
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error...");
            return false;
        }

        // Proceed here if all goes well
        System.out.println("Successfully created customer.");
        return true;
    }

    public static String importCustomers(String lastName, String firstName, String contactNum, String email) {
        Connection conn;
        String newCustomerID = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String sql1 = "SELECT COUNT(*) FROM customers WHERE last_name = ? AND first_name = ? AND contact_num = ? AND email = ?";
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setString(1, lastName);
                pstmt1.setString(2, firstName);
                pstmt1.setString(3, contactNum);
                pstmt1.setString(4, email);
                ResultSet rs1 = pstmt1.executeQuery();
                if (rs1.next()) {
                    if(rs1.getInt(1) > 0){
                        System.out.println("Customer Already Exists in Database");
                    } else{
                        String sql2 = "INSERT INTO customers (last_name, first_name, contact_num, email) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            pstmt2.setString(1, lastName);
                            pstmt2.setString(2, firstName);
                            pstmt2.setString(3, contactNum);
                            pstmt2.setString(4, email);
                            pstmt2.executeUpdate();
                            System.out.println("Customer Imported Successfully...");
                            conn.commit();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("!Error...");
                        }
                    }

                    // Get the customer id present in the db (regardless if existing or not) 
                    String sql3 = "SELECT customer_id FROM customers WHERE last_name = ? AND first_name = ? AND contact_num = ? AND email = ?";
                    PreparedStatement pstmt3 = conn.prepareStatement(sql3);
                    pstmt3.setString(1, lastName);
                    pstmt3.setString(2, firstName);
                    pstmt3.setString(3, contactNum);
                    pstmt3.setString(4, email);

                    ResultSet rs2 = pstmt3.executeQuery();
                    rs2.next();
                    newCustomerID = String.valueOf(rs2.getInt("customer_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("!Error...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in importCustomers");
        }   
        return newCustomerID;
    }
    
    public static void importStaff(String lastName, String firstName, String password, String email) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String sql = "SELECT COUNT(*) FROM staff WHERE last_name = ? AND first_name = ? AND password = ? AND email = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, lastName);
                pstmt.setString(2, firstName);
                pstmt.setString(3, password);
                pstmt.setString(4, email);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if(rs.getInt(1) > 0){
                        System.out.println("Staff Already Exists in Database");
                    } else{
                        String sql2 = "INSERT INTO staff (last_name, first_name, password, email) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            pstmt2.setString(1, lastName);
                            pstmt2.setString(2, firstName);
                            pstmt2.setString(3, password);
                            pstmt2.setString(4, email);
                            pstmt2.executeUpdate();
                            System.out.println("Staff Imported Successfully...");
                            conn.commit();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("!Error...");
                        }
                    }
                }
            } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("!Error...");
                        }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in importStaff");
        }   
    }

    public static String importBooking(String uuid, int customerId, int roomId, String checkInDate, String checkOutDate, String bookingDate, String bookingStatus){
        Connection conn;
        String newBookingID = null;
        try{
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            System.out.println(" Connection established...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String sql = "SELECT COUNT(*) FROM bookings WHERE booking_uuid = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, uuid);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if(rs.getInt(1) > 0){
                        System.out.println("Booking Already Exists in Database");
                    } else{
                        String sql2 = "INSERT INTO bookings (booking_uuid, customer_id, room_id, check_in_date, check_out_date, booking_date, booking_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        releaseDatabaseLocks();
                        System.out.println(" Releasing database locks...");
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            System.out.println(" Preparing statement...");
                            pstmt2.setString(1, uuid);
                            pstmt2.setInt(2, customerId);
                            pstmt2.setInt(3, roomId);
                            pstmt2.setDate(4, java.sql.Date.valueOf(checkInDate));
                            pstmt2.setDate(5, java.sql.Date.valueOf(checkOutDate));
                            pstmt2.setTimestamp(6, java.sql.Timestamp.valueOf(bookingDate));
                            pstmt2.setString(7, bookingStatus.toLowerCase());
                            System.out.println("Customer ID:" + customerId);
                            System.out.println("Room ID:" + roomId);
                            System.out.println("Check In Date:" + java.sql.Date.valueOf(checkInDate));
                            System.out.println("Check Out Date:" + java.sql.Date.valueOf(checkOutDate));
                            System.out.println("Booking Date:" + java.sql.Timestamp.valueOf(bookingDate));
                            System.out.println("Booking Status:" + bookingStatus.toLowerCase());
                            System.out.println(" Executing statement...");
                            pstmt2.executeUpdate();
                            System.out.println(" Finished executing...");

                            conn.commit();
                        }
                    }
                }
                // Get the customer id present in the db (regardless if existing or not) 
                String sql3 = "SELECT booking_id FROM bookings WHERE booking_uuid = ? AND customer_id = ? AND room_id = ? AND check_in_date = ? AND check_out_date = ? AND booking_date = ? AND booking_status = ?";
                PreparedStatement pstmt3 = conn.prepareStatement(sql3);
                pstmt3.setString(1, uuid);
                pstmt3.setInt(2, customerId);
                pstmt3.setInt(3, roomId);
                pstmt3.setDate(4, java.sql.Date.valueOf(checkInDate));
                pstmt3.setDate(5, java.sql.Date.valueOf(checkOutDate));
                pstmt3.setTimestamp(6, java.sql.Timestamp.valueOf(bookingDate));
                pstmt3.setString(7, bookingStatus.toLowerCase());

                ResultSet rs2 = pstmt3.executeQuery();
                rs2.next();
                newBookingID = String.valueOf(rs2.getInt("booking_id"));

            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
                System.out.println("!Error...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in importBooking");
        }
        return newBookingID;
    }

    public static void importPayments(int bookingId, double amount, String paymentDate, String paymentMethod){
        Connection conn;
        try{
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            System.out.println(" Connection established...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String sql = "SELECT COUNT(*) FROM payments WHERE booking_id = ? AND amount = ? AND payment_date = ? AND payment_method = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookingId);
                pstmt.setDouble(2, amount);
                pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(paymentDate));
                pstmt.setString(4, paymentMethod);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    if(rs.getInt(1) > 0){
                        System.out.println("Payment Already Exists in Database");
                    } else{
                        String sql2 = "INSERT INTO payments (booking_id, amount, payment_date, payment_method) VALUES (?, ?, ?, ?)";
                        releaseDatabaseLocks();
                        System.out.println(" Releasing database locks...");
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            System.out.println(" Preparing statement...");
                            pstmt2.setInt(1, bookingId);
                            pstmt2.setDouble(2, amount);
                            pstmt2.setTimestamp(3, java.sql.Timestamp.valueOf(paymentDate));
                            pstmt2.setString(4, paymentMethod);
                            System.out.println("Booking ID:" + bookingId);
                            System.out.println("Amount:" + amount);
                            System.out.println("Payment Date" + java.sql.Timestamp.valueOf(paymentDate));
                            System.out.println("Payment Method:" + paymentMethod);
                            System.out.println(" Executing statement...");
                            pstmt2.executeUpdate();
                            System.out.println(" Finished executing...");

                            conn.commit();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
                System.out.println("!Error...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in importPayments");
        }
    }

    public static String importRooms(String roomNumber, String roomType, String roomStatus){
        Connection conn;
        String newRoomID = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");

            String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roomNumber);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if(rs.getInt(1) > 0){
                        System.out.println("Room Already Exists in Database");
                    } else{
                        String sql2 = "INSERT INTO rooms (room_number, type, room_status) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            pstmt2.setString(1, roomNumber);
                            pstmt2.setString(2, roomType);
                            pstmt2.setString(3, roomStatus);
                            pstmt2.executeUpdate();
                            System.out.println("Room Imported Successfully...");
                            conn.commit();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("!Error...");
                        }
                    }
                    // Get the customer id present in the db (regardless if existing or not) 
                    String sql3 = "SELECT room_id FROM rooms WHERE room_number = ? AND type = ? AND room_status = ?";
                    PreparedStatement pstmt3 = conn.prepareStatement(sql3);
                    pstmt3.setString(1, roomNumber);
                    pstmt3.setString(2, roomType);
                    pstmt3.setString(3, roomStatus);

                    ResultSet rs2 = pstmt3.executeQuery();
                    rs2.next();
                    newRoomID = String.valueOf(rs2.getInt("room_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("!Error...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in importRooms");
        }
        return newRoomID;
    }
    
    public static boolean createNewBook(int customerId, int roomId, String checkInDate, String checkOutDate,
            String bookingStatus, String email) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            System.out.println(" Connection established...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
            System.out.println("Booking ID: " + String.valueOf(CrudAndOthers.getBookId(customerId)));
            System.out.println("Customer ID: " + customerId);
            UUID uuid = UUID.randomUUID();
            System.out.println("Full UUID: " + uuid);
            String shortenedUuid = uuid.toString().replace("-", "").substring(0, 15);
            System.out.println("UUID: " + shortenedUuid);

            String sql = "INSERT INTO bookings (booking_uuid, customer_id, room_id, check_in_date, check_out_date, booking_status) VALUES (?, ?, ?, ?, ?, ?)";

            releaseDatabaseLocks();
            System.out.println(" Releasing database locks...");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                System.out.println(" Preparing statement...");
                pstmt.setString(1, shortenedUuid);
                pstmt.setInt(2, customerId);
                pstmt.setInt(3, roomId);
                pstmt.setDate(4, java.sql.Date.valueOf(checkInDate));
                pstmt.setDate(5, java.sql.Date.valueOf(checkOutDate));
                pstmt.setString(6, bookingStatus.toLowerCase());
                System.out.println("Customer ID:" + customerId);
                System.out.println("Room ID:" + roomId);
                System.out.println("Check In Date:" + java.sql.Date.valueOf(checkInDate));
                System.out.println("Check Out Date:" + java.sql.Date.valueOf(checkOutDate));
                System.out.println("Booking Status:" + bookingStatus.toLowerCase());
                System.out.println(" Executing statement...");
                pstmt.executeUpdate();
                System.out.println(" Finished executing...");

                conn.commit();
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
                System.out.println("!Error...");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createNewBook");
            return false;
        }
    }

    public static boolean createPayments(int bookingId, Double amount, String paymentMethod) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createPayments");
            return false;
        }

        String sql = "INSERT INTO payments (booking_id, amount, payment_method) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, paymentMethod.toLowerCase());
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            System.out.println("!Error...");
            return false;
        }

        // Proceed here if all goes well
        System.out.println("Successfully created payment.");
        return true;
    }

    public static boolean createRoom(int room_number, String type, String layout, int capacity, int price,
            String room_status) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createRoom");
            return false;
        }

        String query = "INSERT INTO rooms (room_number, type, layout, capacity, price, room_status) Values(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, room_number);
            pstmt.setString(2, type);
            pstmt.setString(3, layout);
            pstmt.setInt(4, capacity);
            pstmt.setInt(5, price);
            pstmt.setString(6, room_status);

            pstmt.executeQuery(query);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            System.out.println("!Error...");
            return false;
        }

        // Proceed here if all goes well
        System.out.println("Successfully created room.");
        return true;
    }

    public static boolean createStaffAccount(String lastName, String firstName, String password, String email)
            throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in createStaffAccount");
            return false;
        }

        String sql = "INSERT INTO staff (first_name, last_name, password, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, password);
            pstmt.setString(4, email);

            pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            System.out.println("!Error...");
            return false;
        }

        // Proceed here if all goes well
        System.out.println("Successfully created staff.");
        return true;
    }

    // --- OTHER METHODS ---
    public static void printAvailableRooms(List<Room> availableRooms) {
        System.out.println("Available Rooms:");
        if (availableRooms.isEmpty()) {
            System.out.println("No available rooms found matching criteria.");
        } else {
            for (Room room : availableRooms) {
                System.out.println(room);
            }
            System.out.println("Total available rooms: " + availableRooms.size());
        }
    }

    public static boolean staffLogIn(String email, String password) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in staffLogIn");
            return false;
        }

        String sql = "SELECT * FROM staff WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // matching record found
        } catch (SQLException e) {
            System.out.println("!Error during staff login: " + e.getMessage());
            return false;
        }
    }

    // Returns -1 if !Error occurs, expected value if otherwise
    public int bookedRooms(String date, String roomType) {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in bookedRooms");
            return -1;
        }
        int bookedRooms = 0;

        String query = "SELECT COUNT(*) AS booked_count " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.room_id " +
                "WHERE b.booking_status = 'confirmed' " +
                "AND r.type = ? " +
                "AND ? >= b.check_in_date " +
                "AND ? < b.check_out_date";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomType);
            pstmt.setString(2, date);
            pstmt.setString(3, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bookedRooms = rs.getInt("booked_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in bookedRooms");
            return -1;
        }
        return bookedRooms;
    }

    public static boolean openRoomForBooking(int bookingId) throws SQLException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in openRoomForBooking");
            return false;
        }
        String selectQuery = "SELECT r.room_id, r.room_status FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.room_id " +
                "WHERE b.booking_id = ?";

        String updateQuery = "UPDATE rooms SET room_status = 'available' WHERE room_id = ? AND room_status = 'unavailable'";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, bookingId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String currentStatus = rs.getString("room_status");

                    // check if the room is currently unavailable
                    if (!"unavailable".equals(currentStatus)) {
                        System.out.println("Room is already in '" + currentStatus + "' status, not 'unavailable'");
                        return false;
                    }

                    // update the room status to available
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, roomId);
                        int rowsAffected = updateStmt.executeUpdate();

                        // commit the transaction if successful
                        if (rowsAffected > 0) {
                            conn.commit();
                            return true;
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                } else {
                    System.out.println("No booking found with ID: " + bookingId);
                    return false;
                }
            }
        } catch (SQLException e) {
            // rollback in case of !Error
            conn.rollback();
            System.err.println("!Error opening room for booking: " + e.getMessage());
            throw e;
        }
    }

    public static boolean isRoomAvailable(int roomId, String roomType, String layout, String checkInDate,
            String checkOutDate) {
        List<Room> availableRooms = getAvailableRooms(roomType, checkInDate, checkOutDate);
        for (Room room : availableRooms)
            if (room.roomId == roomId) {
                return true;
            }
        return false;
    }

    public static void releaseDatabaseLocks() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            Statement stmt = conn.createStatement();

            // Step 1: Get the list of processes holding locks
            String query = "SHOW PROCESSLIST";
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("Checking for locked processes...");
            while (rs.next()) {
                int processId = rs.getInt("Id");
                String state = rs.getString("State");
                String info = rs.getString("Info");

                // Check if the process is waiting for a lock
                if (state != null && state.toLowerCase().contains("waiting for")) {
                    System.out.println("Found locked process: ID=" + processId + ", State=" + state + ", Info=" + info);

                    // Step 2: Kill the process to release the lock
                    String killQuery = "KILL " + processId;
                    try {
                        stmt.execute(killQuery);
                        System.out.println("Killed process ID=" + processId + " to release lock.");
                    } catch (SQLException e) {
                        System.out.println("Failed to kill process ID=" + processId + ": " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error while releasing database locks: " + e.getMessage());
        } finally {
            if (conn == null) {
                return;
            }

            // if conn is not null
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean executeSqlFile(String SQL_PATH) throws SQLException { // Courtesy: geeksforgeeks.org
        Connection conn;
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("USE hotelbookingmanagement");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("!Error in executeSqlFile");
            return false;
        }

        try (FileReader reader = new FileReader(SQL_PATH)) {
            // Wrap the FileReader in a BufferedReader for efficient reading.

            BufferedReader bufferedReader = new BufferedReader(reader);
            // Create a statement object to execute SQL commands.

            Statement statement = conn.createStatement();
            System.out.println("Executing commands at : " + SQL_PATH);
            StringBuilder builder = new StringBuilder();
            String line;
            int lineNumber = 0;
            int count = 0;

            // Read lines from the SQL file until the end of the file is reached.
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber += 1;
                line = line.trim();

                // Skip empty lines and single-line comments.
                if (line.isEmpty() || line.startsWith("--"))
                    continue;

                builder.append(line);
                // If the line ends with a semicolon, it indicates the end of an SQL command.
                if (line.endsWith(";"))
                    try {
                        statement.execute(builder.toString()); // execute the SQL command
                        // Print a success message along with the first 15 characters of the executed
                        // command.
                        System.out.println(
                                ++count
                                        + " Command successfully executed : "
                                        + builder.substring(0, Math.min(builder.length(), 15))
                                        + "...");
                        builder.setLength(0);
                    } catch (SQLException e) {
                        // If an SQLException occurs during
                        // execution, print an !Error message and
                        // stop further execution.
                        System.err.println("At line " + lineNumber + " : " + e.getMessage() + "\n");
                        return false;
                    }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            System.out.println("!Error...");
            e.printStackTrace();
            return false;
        }

        // Proceed here if all goes well
        System.out.println("All commands executed successfully...");
        return true;
    }

}
