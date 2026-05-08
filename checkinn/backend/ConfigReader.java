package backend;

import java.io.*;
import java.util.Properties;

public class ConfigReader {
    private static final String ENV_FILE = ".env";
    private Properties properties;

    // Constructor: loads/reads the config file
    public ConfigReader() {
        properties = new Properties();
        loadConfig();
    }

    // Loads the config.env file
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream(ENV_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            // env file DNE
            System.err.println("!Error reading .env file: " + e.getMessage());
            createDefaultConfig();
        }
    }

    // Creating a config file
    private void createDefaultConfig() {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("DB_URL", "jdbc:mysql://localhost/");
        defaultProps.setProperty("DB_USERNAME", "root");
        defaultProps.setProperty("DB_PASSWORD", "");

        try (FileOutputStream fos = new FileOutputStream(ENV_FILE)) {
            defaultProps.store(fos, "Database Configuration");
            properties = defaultProps;
            System.out.println("Created default .env file. Please edit " + ENV_FILE + " with your database credentials.");
        } catch (IOException e) {
            System.err.println("!Error creating default .env file: " + e.getMessage());
        }
    }

    // Gets the database URL 
    public String getDbUrl() {
        return properties.getProperty("DB_URL", "jdbc:mysql://localhost/");
    }

    // Gets the database username
    public String getDbUsername() {
        return properties.getProperty("DB_USERNAME", "root");
    }

    // Gets the database password
    public String getDbPassword() {
        return properties.getProperty("DB_PASSWORD", "");
    }

    public void updateConfig(String username, String password) {
        properties.setProperty("DB_USERNAME", username);
        properties.setProperty("DB_PASSWORD", password);
        CrudAndOthers.setConnectionDetails("jdbc:mysql://localhost/", username, password);

        try (FileOutputStream fos = new FileOutputStream(ENV_FILE)) {
            properties.store(fos, "Database Configuration");
            System.out.println("Configuration updated successfully.");
        } catch (IOException e) {
            System.err.println("!Error updating .env file: " + e.getMessage());
        }
    }
}