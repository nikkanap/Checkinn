package frontend;

import backend.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Booking: Calendar and Availability
public class Booking extends JPanel {
    private Image bg_image;
    private JPanel cardPanel, datesPanel;
    private Booking booking;
    private CardLayout cardLayout;
    private String checkInDate, checkOutDate;
    private JLabel showCheckIn, showCheckOut;
    private RightPanel rightPanel;
    private Boolean updatedAvailability;

    public Booking(CardLayout cardLayout, JPanel cardPanel) throws SQLException {

        System.out.println("Booking");
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.booking = this;

        // Set background image and panel setup
        bg_image = new ImageIcon("./files/booking.png").getImage();
        panelSetup();
        add(new HeaderPanel(), BorderLayout.NORTH);

        // Create centerPanel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerPanel.setOpaque(false);

        // Create leftPanel and rightPanel
        LeftPanel leftPanel = new LeftPanel();
        leftPanel.clearAllHighlights(null);

        // Add leftPanel and rightPanel to centerPanel
        this.rightPanel = new RightPanel();
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    // Create leftPanel
    private class LeftPanel extends JPanel implements ActionListener {

        private JPanel calendarPanel, checkPanel, btns2JPanel;
        private JButton prevButton, nextButton, resetButton, checkAvailButton, dayButtons[];
        private YearMonth month;
        private LocalDate firstOfMonth;
        private int daysInMonth, firstDayOfWeek, monthValue, checkInDay, checkOutDay, checkInMonth, checkInYear;
        private boolean checkInSelect;
        private LocalDate today;

        // Add panels to leftPanel
        public LeftPanel() {

            // Set leftPanel dimension
            setPreferredSize(new Dimension(350, 470));
            setOpaque(false);

            // Set variables
            checkInSelect = true;
            updatedAvailability = false;
            today = LocalDate.now();
            monthValue = 0;

            // Create calendar and buttons
            calendarPanel = setCalendar();
            checkPanel = setCheckJPanel();
            btns2JPanel = setBtns2JPanel();

            // Populate calendar with buttons
            populateDayButtons(datesPanel);
            JPanel[] panels = new JPanel[] { calendarPanel, checkPanel, btns2JPanel };
            for (JPanel panel : panels) {
                add(panel);
            }
        }

        // Create calendar content
        private JPanel setCalendar() {

            // Calendar JPanel holding the calendar contents
            JPanel calendar = new JPanel();
            calendar.setPreferredSize(new Dimension(250, 270));
            calendar.setOpaque(false);

            // JLabel for "Select date"
            Font customFont1 = CustomFont.LoadCustomFont(13);
            JLabel selectDate = new JLabel("Select date:");
            selectDate.setFont(customFont1);
            selectDate.setPreferredSize(new Dimension(250, 20));

            // Setting up the dates
            setUpDate();

            // Month name label
            String monthString = month.getMonth().toString() + " (" + month.getYear() + ")";
            JLabel monthLabel = new JLabel(monthString, SwingConstants.CENTER);
            monthLabel.setPreferredSize(new Dimension(250, 20));
            monthLabel.setOpaque(true);
            monthLabel.setBackground(new Color(0x4a75e8));
            monthLabel.setForeground(Color.WHITE);
            calendar.add(monthLabel);

            // Dates panel with GridLayout
            datesPanel = new JPanel();
            datesPanel.setOpaque(false);
            datesPanel.setLayout(new GridLayout(0, 7)); // 7 days per week
            datesPanel.setPreferredSize(new Dimension(250, 160));

            // Populate calendar with dates
            populateDayButtons(datesPanel);
            calendar.add(datesPanel);
            repaint();
            revalidate();

            // Create Prev/Next Buttons panel
            JPanel btns1JPanel = new JPanel();
            btns1JPanel.setOpaque(false);
            btns1JPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 55, 0));
            btns1JPanel.setPreferredSize(new Dimension(250, 50));
            prevButton = new CustomButton("Prev", 13, 70, 30, new Color(0x4a75e8), new Color(0xececec));
            nextButton = new CustomButton("Next", 13, 70, 30, new Color(0x4a75e8), new Color(0xececec));

            // Disable prev button if current month is displayed
            if (monthValue <= 0 && YearMonth.now().equals(month)) {
                prevButton.setEnabled(false);
            }

            // Add action listener to prev and next buttons
            prevButton.addActionListener(this);
            nextButton.addActionListener(this);

            // Add prev and next buttons to btns1JPanel
            btns1JPanel.add(prevButton);
            btns1JPanel.add(nextButton);

            // Add btns1JPanel to calendar panel
            calendar.add(btns1JPanel);
            return calendar;
        }

        // Set up date for calendar panel
        private void setUpDate() {
            month = YearMonth.now().plusMonths(monthValue);
            daysInMonth = month.lengthOfMonth();
            firstOfMonth = month.atDay(1);

            // 1 = Monday, 7 = Sunday
            firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        }

        // Populate datesPanel with days Jbuttons
        private void populateDayButtons(JPanel datesPanel) {
            datesPanel.removeAll(); // Clear previous buttons

            // Weekday headers
            Font customFont2 = CustomFont.LoadCustomFont(12);
            String[] days = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
            for (String dayName : days) {
                JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
                dayLabel.setForeground(Color.BLACK);
                dayLabel.setFont(customFont2);
                datesPanel.add(dayLabel);
            }

            // Add empty labels before first day (adjusting for Sunday = 7 in ISO)
            int emptyDays = (firstDayOfWeek % 7); // Make Sunday = 0, Monday = 1, ..., Saturday = 6
            for (int i = 0; i < emptyDays; i++) {
                datesPanel.add(new JLabel(""));
            }

            dayButtons = new JButton[daysInMonth];
            Map<LocalDate, Integer> availabilityMap = new HashMap<>();

            // Fetch availability data
            try (Connection connection = DriverManager.getConnection(CrudAndOthers.getURL(), CrudAndOthers.getUSER(),
                    CrudAndOthers.getPASS())) {
                availabilityMap = CrudAndOthers.getAvailableRoomsCountForMonth(month, connection);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error checking availability: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            System.out.println(availabilityMap);

            // Create buttons for each day
            for (int day = 1; day <= daysInMonth; day++) {
                dayButtons[day - 1] = new JButton(String.valueOf(day));
                dayButtons[day - 1].setOpaque(false);
                dayButtons[day - 1].setBorder(null);
                dayButtons[day - 1].setBackground(new Color(0x4a75e8));
                dayButtons[day - 1].setForeground(Color.DARK_GRAY);
                dayButtons[day - 1].setFocusPainted(false); // Remove focus indication
                dayButtons[day - 1].setContentAreaFilled(false); // Remove button background

                // Check if this date is in the past
                LocalDate buttonDate = month.atDay(day);
                if (buttonDate.isBefore(today)) {
                    dayButtons[day - 1].setEnabled(false);
                    dayButtons[day - 1].setForeground(Color.LIGHT_GRAY);
                } else {
                    dayButtons[day - 1].addActionListener(this);

                    // Update button based on availability - ONLY for future dates
                    int bookedRooms = availabilityMap.getOrDefault(buttonDate, 0);
                    if (bookedRooms == 0) {
                        dayButtons[day - 1].setForeground(Color.RED);
                        dayButtons[day - 1].setEnabled(true);
                    } else if (bookedRooms == 2) {
                        dayButtons[day - 1].setEnabled(true);
                        dayButtons[day - 1].setForeground(new Color(200, 200, 0));
                    } else if (bookedRooms == 1) {
                        dayButtons[day - 1].setEnabled(true);
                        dayButtons[day - 1].setForeground(Color.DARK_GRAY);
                    }
                }

                datesPanel.add(dayButtons[day - 1]);
            }
            revalidate();
            repaint();

        }

        // Check in/out content
        private JPanel setCheckJPanel() {

            // JPanel for the check in/out fields and labels
            JPanel checkInPanel = new JPanel();
            checkInPanel.setOpaque(false);
            checkInPanel.setLayout(new GridLayout(2, 2, 10, 2));
            checkInPanel.setPreferredSize(new Dimension(250, 50));

            JLabel checkIn = new JLabel("Check In:");
            showCheckIn = new JLabel("N/A");
            showCheckIn.setForeground(new Color(0x191919));

            JLabel checkOut = new JLabel("Check Out:");
            showCheckOut = new JLabel("N/A");
            showCheckOut.setForeground(new Color(0x191919));

            // Add Jlabels to panel
            checkInPanel.add(checkIn);
            checkInPanel.add(checkOut);
            checkInPanel.add(showCheckIn);
            checkInPanel.add(showCheckOut);
            return checkInPanel;
        }

        // Lower buttons content
        private JPanel setBtns2JPanel() {

            // Create panel for reset and check availability buttons
            JPanel btns2JPanel = new JPanel();
            btns2JPanel.setPreferredSize(new Dimension(250, 50));
            btns2JPanel.setLayout(new GridLayout(2, 1, 0, 5));

            // Create reset and check availability buttons
            resetButton = new CustomButton("Reset", 13, 100, 50, new Color(0xd9d9d9), new Color(0x000000));
            resetButton.addActionListener(this);
            checkAvailButton = new CustomButton("Check availability", 13, 100, 50, new Color(0x4a75e8),
                    new Color(0xf9f9f9));
            checkAvailButton.addActionListener(this);

            // Add buttons to panel
            btns2JPanel.add(resetButton);
            btns2JPanel.add(checkAvailButton);
            return btns2JPanel;
        }

        // Highlight selected dates of user on calendar
        private void highlightDates() {

            // Only highlight dates if we have both check-in and check-out
            System.out.println("checkInDate: " + checkInDate + " checkOutDate: " + checkOutDate);
            if (checkInDate != null && checkOutDate != null) {
                LocalDate checkIn = LocalDate.parse(checkInDate);
                LocalDate checkOut = LocalDate.parse(checkOutDate);

                if (dayButtons == null || dayButtons.length != daysInMonth) {
                    System.out.println("Day buttons array is invalid, skipping highlight");
                    return;
                }

                for (JButton dayButton : dayButtons) {
                    try {
                        int day = Integer.parseInt(dayButton.getText());
                        LocalDate buttonDate = month.atDay(day);
                        if (!buttonDate.isBefore(checkIn) && !buttonDate.isAfter(checkOut)) {
                            System.out.println("Highlighting: " + buttonDate);
                            dayButton.setOpaque(true);
                            dayButton.setContentAreaFilled(true);
                            dayButton.setForeground(Color.WHITE);
                        } else {
                            clearDateHighlights(dayButton, checkIn);
                        }
                    } catch (NumberFormatException e) {
                        clearDateHighlights(dayButton, null);
                    }
                }
            }
            revalidate();
            repaint();

        }

        // Clear highlights for dates before the selected check in date
        private void clearDateHighlights(JButton dayButton, LocalDate checkInDate) {
            dayButton.setOpaque(false);
            dayButton.setContentAreaFilled(false);
            dayButton.setForeground(Color.DARK_GRAY);

            try {
                int day = Integer.parseInt(dayButton.getText());
                LocalDate buttonDate = month.atDay(day);
                if (checkInDate == null) {
                    if (buttonDate.isBefore(today)) {
                        dayButton.setEnabled(false);
                        dayButton.setForeground(Color.LIGHT_GRAY);
                    } else {
                        dayButton.setEnabled(true);
                        dayButton.setForeground(Color.BLACK);
                    }
                } else {
                    System.out.println("Clearing Date before Check In Date: " + checkInDate);
                    if (buttonDate.isBefore(checkInDate)) {
                        dayButton.setEnabled(false);
                        dayButton.setForeground(Color.LIGHT_GRAY);
                    } else {
                        dayButton.setEnabled(true);
                    }

                }

            } catch (NumberFormatException e) {

            }
        }

        // Clear highlights and repaint
        private void clearAllHighlights(LocalDate checkInDate) {
            for (JButton dayButton : dayButtons) {
                clearDateHighlights(dayButton, checkInDate);
            }
            revalidate();
            repaint();
        }

        // Action listener for all buttons on leftPanel
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == prevButton || e.getSource() == nextButton) {
                monthValue += (e.getSource() == prevButton) ? -1 : 1;

                removeAll(); // Remove everything from this panel

                // Recreate panels with updated monthValue
                calendarPanel = setCalendar();
                if (checkInDate != null || checkOutDate != null) {
                    clearAllHighlights(null);
                    highlightDates();
                } else {
                    clearAllHighlights(null);
                }

                JPanel[] panels = new JPanel[] { calendarPanel, checkPanel, btns2JPanel };
                for (JPanel panel : panels) {
                    add(panel);
                }

                revalidate(); // Ask layout manager to re-layout
                repaint(); // Repaint the updated UI

                // Reset selection if changing months
                if (!checkInSelect) {
                    checkInSelect = true;
                }

            } else if (e.getSource() == resetButton) {
                checkInSelect = true;
                checkInDay = 0;
                checkOutDay = 0;
                showCheckIn.setText("N/A");
                showCheckOut.setText("N/A");
                clearAllHighlights(null);
                populateDayButtons(datesPanel);

                // reset dates to null
                checkInDate = null;
                checkOutDate = null;
                try {
                    rightPanel.updateInnerPanel();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error checking availability: " + e1.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else if (e.getSource() == checkAvailButton) {
                // Error checking for no check-in/out dates added
                if (showCheckIn.getText().equals("N/A") || showCheckOut.getText().equals("N/A")) {
                    JOptionPane.showMessageDialog(this,
                            "Please select both check-in and check-out dates",
                            "Missing Dates",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // For convenience sake: (convert showCheckIn/Out JLabels to string)
                String getCheckInString = showCheckIn.getText();
                String getCheckOutString = showCheckOut.getText();

                // Converts the string checkIn/Out dates (sets to NULL to fit branching
                // conditions)
                checkInDate = convertDate((getCheckInString.equals("N/A")) ? null : getCheckInString);
                checkOutDate = convertDate((getCheckOutString.equals("N/A")) ? null : getCheckOutString);
                System.out.println("Check In Date: " + checkInDate);
                System.out.println("Check Out Date: " + checkOutDate);

                // Tells us that the availability has been updated to the latest date (will be
                // set to false when check in/out dates are changed and not checked of its
                // availability)
                updatedAvailability = true;

                try {
                    rightPanel.updateInnerPanel();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error checking availability: " + e1.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Handle day button clicks
                for (JButton dayButton : dayButtons) {
                    if (e.getSource() == dayButton) {
                        if (showCheckIn.getText().equals("N/A")) {
                            checkInSelect = true;
                        } else {
                            checkInSelect = false;
                        }
                        // Set updatedAvailability to false since we're changing out check-in/out dates
                        updatedAvailability = false;

                        // Get the necessary date information
                        String monthString = month.getMonth().toString();
                        String day = dayButton.getText();
                        LocalDate buttonDate = month.atDay(Integer.valueOf(day));
                        int year = month.getYear();
                        int selectedDay = Integer.valueOf(day);
                        int selectedMonth = month.getMonthValue();
                        int selectedYear = month.getYear();

                        // Check-in selection
                        if (checkInSelect) {
                            // Clear all previous highlights
                            System.out.println("Button Date: " + buttonDate);
                            clearAllHighlights(buttonDate);

                            // Highlight just this button
                            dayButton.setOpaque(true);
                            dayButton.setForeground(Color.WHITE);

                            String checkInString = monthString + " " + day + ", " + year;
                            showCheckIn.setText(checkInString);
                            showCheckOut.setText("N/A");
                            showCheckIn.setForeground(new Color(0x191919));
                            showCheckOut.setForeground(new Color(0x191919));
                            checkInDay = selectedDay;
                            checkInMonth = selectedMonth;
                            checkInYear = selectedYear;
                            checkOutDay = 0; // Reset check-out day
                            checkInSelect = false;
                            return;
                        }

                        // Check-out selection - ensure it's after check-in
                        if (selectedYear < checkInYear) {
                            JOptionPane.showMessageDialog(this,
                                    "Check-out date must be after check-in date",
                                    "Invalid Selection",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        } else {
                            if (selectedMonth < checkInMonth) {
                                JOptionPane.showMessageDialog(this,
                                        "Check-out date must be after check-in date",
                                        "Invalid Selection",
                                        JOptionPane.WARNING_MESSAGE);
                                return;
                            } else {
                                if (selectedDay <= checkInDay && selectedMonth == checkInMonth) {
                                    JOptionPane.showMessageDialog(this,
                                            "Check-out date must be after check-in date",
                                            "Invalid Selection",
                                            JOptionPane.WARNING_MESSAGE);
                                    return;
                                }
                            }
                        }

                        String checkOutString = monthString + " " + day + ", " + year;
                        showCheckOut.setText(checkOutString);
                        checkOutDay = selectedDay;

                        // Highlight the range
                        System.out.println("Highlighting dates from " + checkInDay + " to " + checkOutDay);

                        // Set dates for availability check
                        checkInDate = convertDate(showCheckIn.getText());
                        checkOutDate = convertDate(showCheckOut.getText());
                        highlightDates();

                        break;
                    }
                }
            }
        }
    }

    // Create rightPanel
    private class RightPanel extends JPanel {
        JPanel innerJPanel;
        private final String[] roomLabels = new String[] { "Standard", "Double", "Deluxe", "Suite" };
        private final String[] iconFilePaths = new String[] {
                "./files/standard.jpg",
                "./files/twin.jpg",
                "./files/deluxe.jpg",
                "./files/suite.jpg"
        };
        private JLabel availableLabel;

        public RightPanel() throws SQLException {
            setPreferredSize(new Dimension(590, 500));
            setLayout(new GridBagLayout());
            setOpaque(false);
            this.innerJPanel = setInnerJPanel();
            add(innerJPanel, new GridBagConstraints());
        }

        private JPanel setInnerJPanel() throws SQLException {
            JPanel innerJPanel = new JPanel();
            innerJPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
            innerJPanel.setOpaque(false);
            innerJPanel.setPreferredSize(new Dimension(550, 470));
            innerJPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            Connection conn = DriverManager.getConnection(CrudAndOthers.getURL(), CrudAndOthers.getUSER(),
                    CrudAndOthers.getPASS());
            gbc.gridy = 0;
            gbc.gridx = 0;
            int ctr = 0;
            for (; gbc.gridy < 2; gbc.gridy++) {
                for (; gbc.gridx < 2; gbc.gridx++) {
                    boolean available = false;
                    if (checkInDate != null && checkOutDate != null &&
                            CrudAndOthers.getAvailableRoomsByType(checkInDate, checkOutDate, roomLabels[ctr],
                                    conn) > 0) {
                        available = true;
                        System.out.println("Available: " + roomLabels[ctr]);
                    } else {
                        available = false;
                    }
                    innerJPanel.add(roomViewJPanel(ctr++, available), gbc);
                }
                gbc.gridx = 0;
            }
            conn.close();
            return innerJPanel;
        }

        // JPanel for viewing the rooms
        private JPanel roomViewJPanel(int ptr, boolean available) {
            Font customFont = CustomFont.LoadCustomFont(13);
            JPanel roomViewJPanel = new JPanel();
            roomViewJPanel.setPreferredSize(new Dimension(250, 200));
            roomViewJPanel.setBackground(new Color(0xd9d9d9));

            // JPanel for labels
            JPanel labelsPanel = new JPanel();
            labelsPanel.setPreferredSize(new Dimension(235, 20));
            labelsPanel.setLayout(new GridLayout(1, 2, 50, 0));
            labelsPanel.setOpaque(false);

            // Set availability label
            availableLabel = new JLabel((available) ? "Available" : "Unavailable");
            availableLabel.setForeground((available) ? new Color(0x248f12) : new Color(0xc12227));
            availableLabel.setFont(customFont);

            // Set roomtype label
            JLabel roomTypeLabel = new JLabel(roomLabels[ptr]);
            roomTypeLabel.setFont(customFont);

            // add the necessary labels to labelsPanel
            labelsPanel.add(roomTypeLabel);
            labelsPanel.add(availableLabel);

            // Set room images
            Image image = new ImageIcon(iconFilePaths[ptr]).getImage();
            Image resizedImage = image.getScaledInstance(235, 125, Image.SCALE_AREA_AVERAGING);
            JLabel roomImage = new JLabel(new ImageIcon(resizedImage));

            // Setting up booking button
            CustomButton bookNow;
            if (available) {
                bookNow = new CustomButton("Book now", 13, 235, 30, new Color(0x4a75e8), new Color(0xececec));
            } else {
                bookNow = new CustomButton("Book now", 13, 235, 30, new Color(0xd9d9d9), new Color(0x535353));
                bookNow.setEnabled(false);
            }
            bookNow.addActionListener(e -> {
                // If room isn't available
                if (!available) {
                    JOptionPane.showMessageDialog(this, "Room is not available", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // If the check-in/out dates have been updated but not checked for availability
                if (!updatedAvailability) {
                    JOptionPane.showMessageDialog(this, "Please update check-in/out dates before booking.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if dates are selected
                if (checkInDate == null || checkOutDate == null) {
                    JOptionPane.showMessageDialog(this, "Please select check-in and check-out dates",
                            "Missing Dates", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // NO ISSUES: Setting up and transitioning to Customer Details JPanel
                CustomerDetails customerDetailsPanel = new CustomerDetails(cardLayout, cardPanel, booking,
                        roomLabels[ptr], checkInDate, checkOutDate,
                        NumberFormat.getNumberInstance(Locale.US).format(CrudAndOthers.getRoomPrice(roomLabels[ptr])));
                cardPanel.add(customerDetailsPanel, "Customer Details");
                cardLayout.show(cardPanel, "Customer Details");
            });

            // add the panels and contents
            roomViewJPanel.add(labelsPanel);
            roomViewJPanel.add(roomImage);
            roomViewJPanel.add(bookNow);

            return roomViewJPanel;
        }

        // To refresh availability of rooms
        public void updateInnerPanel() throws SQLException {
            remove(innerJPanel);
            innerJPanel = setInnerJPanel();
            add(innerJPanel, new GridBagConstraints());
            revalidate();
            repaint();
        }

    }

    // Inner Class: HeaderPanel
    private class HeaderPanel extends JPanel {
        public HeaderPanel() {
            GridBagConstraints gbc = new GridBagConstraints();
            setPreferredSize(new Dimension(900, 100));
            setLayout(new GridBagLayout());
            setOpaque(false);

            CustomButton backButton = new CustomButton("Back", 13, 100, 30, new Color(0xd9d9d9), new Color(0x535353));
            backButton.addActionListener(e -> {
                cardLayout.show(cardPanel, "Main Page");
            });

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(30, 790, 0, 0);
            gbc.anchor = GridBagConstraints.EAST;
            add(backButton, gbc);
        }
    }

    private void panelSetup() {
        setBackground(new CustomBGColor().getBGColor());
        setLayout(new BorderLayout());
    }

    public String convertDate(String inputDate) {
        try {
            if (inputDate == null || inputDate.isEmpty()) {
                return null;
            }
            String normalizedDate = inputDate.substring(0, 1).toUpperCase() + inputDate.substring(1).toLowerCase();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            LocalDate date = LocalDate.parse(normalizedDate, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + inputDate);
            return null;
        }
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
