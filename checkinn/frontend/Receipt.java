package frontend;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import backend.CrudAndOthers;

public class Receipt extends JPanel {

    private Image bg_image;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public Receipt(CardLayout cardLayout, JPanel cardPanel, Booking booking, String firstName, String roomType,
            String checkInDate, String checkOutDate, String bookID) {
        System.out.println("Receipt");
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        
        GridBagConstraints gbc = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        bg_image = new ImageIcon("./files/receipt.png").getImage();

        Font customFont = CustomFont.LoadCustomFont(23);
        JLabel greetingsLabel = new JLabel("Hi, " + firstName);
        greetingsLabel.setFont(customFont);
        greetingsLabel.setForeground(new Color(0x4a75e8));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(120, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTH;
        add(greetingsLabel, gbc);

        JLabel bookLabel = new JLabel("You successfully booked a");
        bookLabel.setFont(customFont);
        bookLabel.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        add(bookLabel, gbc);

        JLabel detailsLabel = new JLabel(roomType + " Room for " + checkInDate + " to " + checkOutDate);
        detailsLabel.setFont(customFont);
        detailsLabel.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(detailsLabel, gbc);

        JLabel bookingIDLabel = new JLabel("Here's your Booking ID");
        bookingIDLabel.setFont(customFont);
        bookingIDLabel.setForeground(new Color(0x4a75e8));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(50, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTH;
        add(bookingIDLabel, gbc);

        
        JLabel bookingID = new JLabel(bookID);
        bookingID.setFont(customFont);
        bookingID.setForeground(new Color(0x191919));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 5, 0);
        add(bookingID, gbc);

        CustomButton bookingButton = new CustomButton("Booking Page", 13, 130, 30, new Color(0xd9d9d9),
                new Color(0x535353));
        bookingButton.addActionListener(e -> {
            cardPanel.remove(booking); // removes the old booking jpanel
            try {
                cardPanel.add(new Booking(cardLayout, cardPanel), "Booking");
                cardLayout.show(cardPanel, "Booking"); // displays the new booking jpanel (reset)
            } catch (SQLException e1) {
                e1.printStackTrace();
            } // adds a new booking jpanel
        });
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(120, 100, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(bookingButton, gbc);

        CustomButton mainpageButton = new CustomButton("Main Page", 13, 100, 30, new Color(0xd9d9d9),
                new Color(0x535353));
        mainpageButton.addActionListener(e -> {
            cardPanel.remove(booking); // removes the old booking jpanel
            try {
                cardPanel.add(new Booking(cardLayout, cardPanel), "Booking");
                cardLayout.show(cardPanel, "Main Page"); // displays the new booking jpanel (reset)
            } catch (SQLException e1) {
                e1.printStackTrace();
            } // adds a new booking jpanel
        });

        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(120, 200, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(mainpageButton, gbc);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg_image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(bg_image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
