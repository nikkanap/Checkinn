package frontend;
import backend.*;

import java.io.File;
import java.sql.*;
import javax.swing.*;

public class Main {
    // Make Sure to add the MySQL Connector JAR file to your project dependencies for this to work.
    // You can download it from https://dev.mysql.com/downloads/connector/j/

    public static void main(String[] args) {
        // initial splashscreen
        JFrame splash = new JFrame("Check-Inn");
        JLabel loadingLabel = new JLabel("Loading...", JLabel.CENTER);
        loadingLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        splash.getContentPane().add(loadingLabel);
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);
        splash.setUndecorated(true);
        splash.setVisible(true);
        
        // read configuration from file
        ConfigReader config = new ConfigReader();
        String url = config.getDbUrl();
        String user = config.getDbUsername();
        String pass = config.getDbPassword();
        
        // hide splash screen
        splash.setVisible(false);
        splash.dispose();
        
        // check if we need to prompt for credentials
        if (!testConnection(url, user, pass)) {
            DbSetupDialog setupDialog = new DbSetupDialog(null, config);
            setupDialog.setVisible(true);
            
            if (!setupDialog.isSetupComplete()) { System.exit(0); }
            
            // get updated credentials
            url = config.getDbUrl();
            user = config.getDbUsername();
            pass = config.getDbPassword();
        }

        System.out.println("Connecting to database...");
        CrudAndOthers.setConnectionDetails(url, user, pass);
        try {
            if (CrudAndOthers.createDatabase()) { // Initialize database if it doesn't exist
                //location is currently at CMSC-127-Hotel-Booking-Management-System

                // If statements executing sql files at different paths for ensurity's sake
                if(!CrudAndOthers.executeSqlFile("\\backend\\database_Schema.sql")){
                    System.out.println("COULD NOT CREATE SCHEMA. Repeating with checkinn directory...");
                    if(!CrudAndOthers.executeSqlFile("checkinn\\backend\\database_Schema.sql")){
                        System.out.println("COULD NOT CREATE SCHEMA");
                        return;
                    } 
                }

                if(!CrudAndOthers.executeSqlFile("backend\\data.sql")){
                    System.out.println("COULD NOT ADD DATA. Repeating with checkinn directory...");
                    if(!CrudAndOthers.executeSqlFile("checkinn\\backend\\data.sql")){
                        System.out.println("COULD NOT ADD DATA");
                        return;
                    } 
                }
                System.out.println("Database and tables created successfully!");
            }
            
            // start the main application
            SwingUtilities.invokeLater(() -> {
                try { new MainFrame(); } 
                catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error in line main");
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null, 
                "Database error: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE
            );
            
            // show the setup dialog again if there was an error
            DbSetupDialog setupDialog = new DbSetupDialog(null, config);
            setupDialog.setVisible(true);
            
            if (setupDialog.isSetupComplete()) { main(args); } // try to restart the application 
            else { System.exit(1); }
        }
    }
    
    private static boolean testConnection(String url, String user, String pass) {
        // if credentials are empty, return false immediately
        if (user.isEmpty() || pass.isEmpty()) { return false; }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) { return true; }
        } catch (ClassNotFoundException | SQLException e) { return false; }
    }
}
