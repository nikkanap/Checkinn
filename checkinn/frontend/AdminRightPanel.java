package frontend;

import java.awt.*;
import javax.swing.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import backend.CrudAndOthers;

public class AdminRightPanel extends JPanel {

    private JPanel roomListPanel;
    private DefaultTableModel model;
    private JTable table;
    private JLabel userLabel;
    private Date searchDate;
    private java.util.Date currentDate;

    public AdminRightPanel() {

        // Update RightPanel each time
        repaint();

        // Set RightPanel size and background
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setPreferredSize(new Dimension(660, 600));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Create inner panel
        JPanel innerPanel = new JPanel();
        innerPanel.setBackground(Color.WHITE);
        innerPanel.setLayout(null);

        // Create header text
        JLabel Title = new JLabel("Booking Details");
        Title.setFont(CustomFont.LoadCustomFont(20));
        Title.setForeground(new Color(0x4a75e8));
        Title.setBounds(0, 0, 400, 30);

        // Create current user text
        userLabel = new JLabel("Current User: ");
        userLabel.setFont(CustomFont.LoadCustomFont(10));
        userLabel.setForeground(Color.BLACK);
        userLabel.setBounds(400, 12, 200, 12);

        // Create note text
        JLabel noteLabel = new JLabel(
                "Click Booking ID and Customer ID to view more information.");
        noteLabel.setFont(CustomFont.LoadCustomFont(10));
        noteLabel.setForeground(new Color(0x535353));
        noteLabel.setBounds(0, 25, 650, 20);

        // Create table model with column editing
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing for column 2 when room status is "unavailable"
                if (column == 2) {
                    String roomStatus = getValueAt(row, 6).toString();
                    return !roomStatus.equals("available");
                }

                // Only allow editing for column 6 when room status is "unavailable"
                if (column == 7) {
                    String roomStatus = getValueAt(row, 6).toString();
                    return !roomStatus.equals("available");
                }
                return false;
            }
        };

        // Create table using model
        table = new JTable(model);
        // Disable column reordering on table
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // Set table column headers
        String[] columnNames = new String[] { "Room No.", "Room Type", "Booking ID", "Booking Date", "Check-in",
                "Check-out",
                "Status",
                "Customer ID" };
        for (String columnName : columnNames)
            model.addColumn(columnName);
        System.out.println("Printing Rooms...");

        // Custom cell renderer for Room Status column (column 5)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String status = value.toString();
                Color availableColor = new Color(144, 255, 151); // Light green
                Color unavailableColor = new Color(255, 139, 139); // Light red

                cell.setBackground(status.equals("available") ? availableColor : unavailableColor);
                cell.setFont(CustomFont.LoadCustomFont(11));

                // Maintain selection color
                if (isSelected) {
                    cell.setBackground(table.getSelectionBackground());
                }
                return cell;
            }
        });

        // Load rooms
        addRooms();
        System.out.println("Rooms added to table");

        // Style table
        table.getTableHeader().setPreferredSize(new Dimension(100, 30));
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));
        table.getTableHeader().setFont(CustomFont.LoadCustomFont(12));
        table.setRowHeight(30);
        table.setFont(CustomFont.LoadCustomFont(11));
        table.setBackground(Color.WHITE);
        table.setGridColor(Color.GRAY);

        // Set column width
        table.getColumnModel().getColumn(0).setPreferredWidth(70); // Room No.
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // Room Type
        table.getColumnModel().getColumn(2).setPreferredWidth(80); // Booking ID
        table.getColumnModel().getColumn(2).setCellRenderer(conditionalLinkRenderer);
        table.getColumnModel().getColumn(2).setCellEditor(new LinkButtonEditor());
        table.getColumnModel().getColumn(3).setPreferredWidth(90); // Booking Date
        table.getColumnModel().getColumn(4).setPreferredWidth(80); // Check-in Date
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Check-out Date
        table.getColumnModel().getColumn(6).setPreferredWidth(85); // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(90); // Customer ID
        table.getColumnModel().getColumn(7).setCellRenderer(conditionalLinkRenderer);
        table.getColumnModel().getColumn(7).setCellEditor(new LinkButtonEditor());

        // Enable grid lines
        table.setShowGrid(true);

        // Wrap table inside scrollable panel
        roomListPanel = new JPanel();
        roomListPanel.setBounds(0, 55, 650, 480);
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(Color.GRAY);

        // Create scrollpane for rooms view
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(650, 480));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        roomListPanel.add(scrollPane);

        // Add everything to innerPanel
        innerPanel.add(Title);
        innerPanel.add(userLabel);
        innerPanel.add(noteLabel);
        innerPanel.add(roomListPanel);
        this.add(innerPanel);
    }

    // Reload room data from database
    public void refreshRooms() {
        model.setRowCount(0); // Clear the existing rows
        addRooms(); // Re-add the rows
    }

    // Sets the date used to filter bookings
    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }

    // Fill table with data
    public void addRooms() {
        ArrayList<String[]> rooms = null;
        try {
            rooms = CrudAndOthers.getAllRooms(searchDate); // Use method from crud and others
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (rooms == null)
            return;

        for (String[] room : rooms) {

            // Check to continue
            if (room.length < 8)
                continue;

            // Set string for customer id
            String customerId = (room[7] == null || room[7].equals("-")) ? "-" : room[7];

            model.addRow(new Object[] {
                    room[0], // Room Number
                    room[1], // Room Type
                    room[2], // Booking ID
                    room[3], // Booking Date
                    room[4], // Check-in
                    room[5], // Check-out
                    room[6], // Room Status
                    customerId
            });
        }
    }

    // Display name of admin logged in
    public void setAdminUser(String email) {
        userLabel.setText("Current User: " + CrudAndOthers.getAdminFullName(email));
        userLabel.setBounds(480 - email.length(), 18, 200, 30);
        userLabel.setFont(CustomFont.LoadCustomFont(12));
        revalidate();
        repaint();
    }

    // Makes the cell act like a button
    class LinkButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private String value;
        private int currentColumn;

        // Initialize button visuals
        public LinkButtonEditor() {
            button = new JButton();
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setForeground(Color.BLACK);
            button.setBackground(Color.LIGHT_GRAY);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFont(CustomFont.LoadCustomFont(11));

            // Button behavior
            button.addActionListener(e -> {
                if (currentColumn == 7) { // Customer ID column action listener
                    showCustomerInfo();
                }
                if (currentColumn == 2) { // Booking ID column action listener
                    showBookingUUID();
                }
                fireEditingStopped(); // Make Jtable stop editing like not clickable
            });
        }

        // HAs the button component to be used in the editor
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.value = value != null ? value.toString() : "-";
            this.currentColumn = column;
            button.setText(this.value);
            return button;
        }

        // Return value
        @Override
        public Object getCellEditorValue() {
            return value;
        }

        // Show customer details in dialog box
        private void showCustomerInfo() {
            if (value.equals("-")) {
                JOptionPane.showMessageDialog(button,
                        "No customer information available.",
                        "Customer Details",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                // Use the getCustomerInfo function with customer ID on crudandothers (backend)
                CrudAndOthers.Customer customer = CrudAndOthers.getCustomerInfo(Integer.parseInt(value));

                if (customer != null) {
                    String customerDetails = String.format(
                            "Customer Information:\n\n" +
                                    "Customer ID: %d\n" +
                                    "Name: %s %s\n" +
                                    "Contact: %s\n" +
                                    "Email: %s",
                            customer.getCustomerId(),
                            customer.getFirstName(),
                            customer.getLastName(),
                            customer.getContactNum(),
                            customer.getEmail());

                    JOptionPane.showMessageDialog(button,
                            customerDetails,
                            "Customer Details",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(button,
                            "Customer not found in database.",
                            "Customer Details",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(button,
                        "Database error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(button,
                        "Invalid customer ID format.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Show UUID in dialog box
        private void showBookingUUID() {
            if (value.equals("-")) {
                JOptionPane.showMessageDialog(button,
                        "No booking information available.",
                        "Booking UUID",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                int bookingId = Integer.parseInt(value);
                String bookingUUID = CrudAndOthers.getBookingUUID(bookingId); // on backend folder

                if (bookingUUID != null) {
                    String message = String.format(
                            "Booking Information:\n\n" +
                                    "Booking ID: %d\n" +
                                    "Booking UUID: %s",
                            bookingId, bookingUUID);

                    JOptionPane.showMessageDialog(button,
                            message,
                            "Booking Details",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(button,
                            "Booking UUID not found in database.",
                            "Booking Details",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(button,
                        "Database error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(button,
                        "Invalid booking ID format.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    // Customize the cell when its not clickable (when column 6 says available)
    DefaultTableCellRenderer conditionalLinkRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = new JLabel();
            // Get room status from column 6 (Status column)
            Object statusObj = table.getValueAt(row, 6);
            String roomStatus = (statusObj != null) ? statusObj.toString() : "available";
            String displayValue = (value != null) ? value.toString() : "-";

            // If room is unavailable, make the cell clickable
            if (!roomStatus.equals("available")) {
                label.setText(displayValue);
                label.setForeground(Color.BLACK);
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                label.setBackground(isSelected ? table.getSelectionBackground() : Color.LIGHT_GRAY);
            } else {
                // If room is available, just normal cell text
                label.setText(displayValue);
                label.setForeground(Color.BLACK);
                label.setCursor(Cursor.getDefaultCursor());
                label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }

            label.setFont(CustomFont.LoadCustomFont(11));
            label.setOpaque(true);
            return label;
        }
    };

}
