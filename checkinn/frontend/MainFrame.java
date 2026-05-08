package frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.net.URL;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    public MainFrame() throws SQLException {
        // Set up the JFrame
        setTitle("Checkinn");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setBounds(100, 50, 900, 600);
        setResizable(false);
        setLayout(new BorderLayout());

        // Sets the java program icon
        URL logoURL = this.getClass().getClassLoader().getResource("files\\logo.png");
        if(logoURL != null){
            setIconImage(Toolkit.getDefaultToolkit().getImage(logoURL)); 
            System.out.println("Successfully loaded in program icon!");
        } else{ System.out.println("URL is null. Failed to load program icon."); }
        
        System.out.println("Creating MainFrame...");
        // Create cardLayout and cardPanel for panel switching
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        // Create initial panels upon startup of program (for ease of switching)
        MainPagePanel mainPagePanel = new MainPagePanel(cardLayout, cardPanel);
        Booking bookingPanel = new Booking(cardLayout, cardPanel);
        CreateNewAdmin createNewAdmin = new CreateNewAdmin(cardLayout, cardPanel);
        AdminPanel adminPanel = new AdminPanel(cardLayout, cardPanel);
        LogIn logInPanel = new LogIn(cardLayout, cardPanel, adminPanel);
        
        // Add all the inialized panels to cardPanel
        cardPanel.add(mainPagePanel, "Main Page");
        cardPanel.add(bookingPanel, "Booking");
        cardPanel.add(createNewAdmin, "Create New Admin");
        cardPanel.add(logInPanel, "Log In");
        cardPanel.add(adminPanel, "Admin Page");
        
        // testing Log In panel
        cardLayout.show(cardPanel, "Main Page"); 

        // add cardPanel to the JFrame
        add(cardPanel);

        // Display the Main Menu panel
        cardLayout.show(cardPanel, "Main Page"); // testing

        // set JFrame visibility to true and pack 
        setVisible(true);
        pack();
    }
}
