package frontend;

import backend.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CustomerDetails extends JPanel implements ActionListener {

    private Image bg_image;
    private JTextField firstNameField, lastNameField, contactField, emailField;
    private JButton confirmButton, backButton;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Booking booking; // needed in Receipt.java to reset booking panel when confirming booking
    private String roomType, checkInDate, checkOutDate, price;

    public CustomerDetails(CardLayout cardLayout, JPanel cardPanel, Booking booking, String roomType,
            String checkInDate, String checkOutDate, String price) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.booking = booking;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.price = price;

        GridBagConstraints gbc = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        bg_image = new ImageIcon("./files/details.png").getImage();

        Font customFont1 = CustomFont.LoadCustomFont(25);
        JLabel bookingInfoLabel = new JLabel("You are trying to book a");
        bookingInfoLabel.setFont(customFont1);
        bookingInfoLabel.setForeground(new Color(0x4a75e8));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(50, 350, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(bookingInfoLabel, gbc);

        JLabel roomLabel = new JLabel(roomType + " room");
        roomLabel.setFont(customFont1);
        roomLabel.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 350, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        add(roomLabel, gbc);

        JLabel forLabel = new JLabel("for");
        forLabel.setFont(customFont1);
        forLabel.setForeground(new Color(0x4a75e8));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 350, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        add(forLabel, gbc);

        JLabel dateLabel = new JLabel(convertToMonthDay(checkInDate) + " - " + convertToMonthDay(checkOutDate));
        dateLabel.setFont(customFont1);
        dateLabel.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 395, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        add(dateLabel, gbc);

        Font customFont2 = CustomFont.LoadCustomFont(20);
        JLabel priceLabel = new JLabel("Price: " + price);
        priceLabel.setFont(customFont2);
        priceLabel.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(12, 365, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        add(priceLabel, gbc);

        JLabel enterLabel = new JLabel("Enter booking details:");
        enterLabel.setFont(customFont2);
        enterLabel.setForeground(new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(38, 370, 0, 0);
        add(enterLabel, gbc);

        Font customFont3 = CustomFont.LoadCustomFont(15);
        JLabel firstName = new JLabel("First Name");
        firstName.setFont(customFont3);
        firstName.setForeground(new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 370, 0, 0);
        add(firstName, gbc);

        firstNameField = new JTextField();
        firstNameField.setPreferredSize(new Dimension(280, 25));
        firstNameField.setFont(customFont3);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 470, 0, 0);
        add(firstNameField, gbc);
        firstNameField.addActionListener(this);

        JLabel lastName = new JLabel("Last Name");
        lastName.setFont(customFont3);
        lastName.setForeground(new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 370, 0, 0);
        add(lastName, gbc);

        lastNameField = new JTextField();
        lastNameField.setPreferredSize(new Dimension(280, 25));
        lastNameField.setFont(customFont3);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 470, 0, 0);
        add(lastNameField, gbc);
        lastNameField.addActionListener(this);

        JLabel contact = new JLabel("Contact");
        contact.setFont(customFont3);
        contact.setForeground(new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 370, 0, 0);
        add(contact, gbc);

        contactField = new JTextField();
        contactField.setPreferredSize(new Dimension(280, 25));
        contactField.setFont(customFont3);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 470, 0, 0);
        add(contactField, gbc);
        contactField.addActionListener(this);

        JLabel email = new JLabel("Email");
        email.setFont(customFont3);
        email.setForeground(new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 370, 0, 0);
        add(email, gbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(280, 25));
        emailField.setFont(customFont3);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 470, 0, 0);
        add(emailField, gbc);
        emailField.addActionListener(this);

        confirmButton = new CustomButton("Confirm", 15, 380, 25, new Color(0x4a75e8), new Color(0xf2f2f2));
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.insets = new Insets(15, 370, 0, 0);
        add(confirmButton, gbc);
        confirmButton.addActionListener(this);

        backButton = new CustomButton("Back", 15, 380, 25, new Color(0xc5c5c5), new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 370, 0, 0);
        add(backButton, gbc);
        backButton.addActionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg_image == null)
            return; // don't proceed

        // if the bg image isn't null:
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(bg_image, 0, 0, getWidth(), getHeight(), this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();

            // Error checking: checks if any of the fields are unfilled
            String[] customerInfo = new String[] { firstName, lastName, contact, email };
            for (String info : customerInfo) {
                if (!info.isBlank()) {
                    continue;
                }
                JOptionPane.showMessageDialog(
                        null,
                        "Please fill out all of the fields.",
                        "Incomplete Fields",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Error checking: limits contact number length to strictly 11
            if (contact.length() != 11 ||!contact.matches("^09\\d{9}$")) {
                JOptionPane.showMessageDialog(
                        null,
                        "Contact Number is invalid (Must be 11 digits and have no spaces).",
                        "Invalid Contact Number",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.endsWith("@gmail.com") || email.length() < "@gmail.com".length() + 3 || email.contains(" ")) {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid email format.",
                        "Invalid Email Format",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // if successful, show all the details
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Contact: " + contact);
            System.out.println("Email: " + email);

            Receipt receiptPanel = null;
            try {
                CrudAndOthers.createCustomers(lastName, firstName, contact, email);
                System.out.println("Customer ID: " + CrudAndOthers.getCustomerId(email));

                List<Room> availableRooms = CrudAndOthers.getAvailableRooms(roomType, checkInDate, checkOutDate);
                CrudAndOthers.printAvailableRooms(availableRooms);
                System.out.println(" Room ID: " + availableRooms.get(0).roomId);

                CrudAndOthers.createNewBook(CrudAndOthers.getCustomerId(email), availableRooms.get(0).roomId,
                        checkInDate, checkOutDate, "Confirmed", email);
                receiptPanel = new Receipt(cardLayout, cardPanel, booking, firstName, roomType, checkInDate,
                        checkOutDate, CrudAndOthers.getBookuuid(CrudAndOthers.getBookId(CrudAndOthers.getCustomerId(email))));
            } catch (SQLException e1) {
                System.out.println("Error in Booking: " + e1.getMessage());
            }
            cardPanel.add(receiptPanel, "Receipt");
            cardLayout.show(cardPanel, "Receipt");
        } else if (e.getSource() == backButton) {
            cardLayout.show(cardPanel, "Booking");
        }
    }

    public String convertToMonthDay(String inputDate) {
        try {
            String normalizedDate = inputDate.substring(0, 1).toUpperCase() + inputDate.substring(1).toLowerCase();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(normalizedDate, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d");
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + inputDate);
            return null;
        }
    }

}
