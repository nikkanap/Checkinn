package frontend;

import backend.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;


public class AdminLeftPanel extends JPanel {

    private AdminRightPanel rightPanel;
    private JTextField datefield;
    private JButton searchButton;
    private RoomStatusPanel standardRooms;
    private RoomStatusPanel doubleRooms;
    private RoomStatusPanel suiteRooms;
    private RoomStatusPanel deluxeRooms;
    private JButton importButton, exportButton, log_out, createNewAdmin;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public AdminLeftPanel(AdminRightPanel rightPanel, CardLayout cardLayout, JPanel cardPanel, Boolean ifSuperAdmin) {

        // Update panel each time
        repaint();
        this.rightPanel = rightPanel;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;

        // Set LeftPanel size, color and border
        setPreferredSize(new Dimension(240, 600));
        setBackground(new Color(0xd9d9d9));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Create inner panel for search and room count panels
        JPanel innerPanel = new JPanel(); // width: 200, height: 550
        innerPanel.setBackground(new Color(0xd9d9d9));
        innerPanel.setLayout(null);

        // Create 'Rooms available' text
        JLabel title = new JLabel("Rooms Available");
        title.setFont(CustomFont.LoadCustomFont(23));
        title.setForeground(new Color(0x000000));
        title.setBounds(0, 0, 200, 30);

        // Create 'Enter date' text
        JLabel enterdate = new JLabel("Enter Date: ");
        enterdate.setFont(CustomFont.LoadCustomFont(13));
        enterdate.setForeground(new Color(0x535353));
        enterdate.setBounds(0, 35, 100, 23);

        // Field for inputting date
        datefield = new JTextField();
        datefield.setBounds(80, 35, 110, 20);

        // Button for searching for that date
        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0xececec));
        searchButton.setForeground(new Color(0x535353));
        searchButton.setFocusable(false);
        searchButton.setBounds(0, 60, 190, 23);

        // Displays search results for Standard, Double, Suite and Deluxe Rooms
        standardRooms = new RoomStatusPanel("Standard");
        standardRooms.setBounds(0, 100, 190, 70);
        doubleRooms = new RoomStatusPanel("Double");
        doubleRooms.setBounds(0, 180, 190, 70);
        suiteRooms = new RoomStatusPanel("Suite");
        suiteRooms.setBounds(0, 260, 190, 70);
        deluxeRooms = new RoomStatusPanel("Deluxe");
        deluxeRooms.setBounds(0, 340, 190, 70);

        if(ifSuperAdmin){
            // Import button
            importButton = new JButton("Import");
            importButton.setFont(CustomFont.LoadCustomFont(13));
            importButton.setBackground(new Color(0x4a75e8));
            importButton.setForeground(Color.WHITE);
            importButton.setBounds(0, 420, 90, 30);

            // Import button
            exportButton = new JButton("Export");
            exportButton.setFont(CustomFont.LoadCustomFont(13));
            exportButton.setBackground(new Color(0x4a75e8));
            exportButton.setForeground(Color.WHITE);
            exportButton.setBounds(100, 420, 90, 30);

            // Create 'Create new admin' button
            createNewAdmin = new JButton("Create New Admin");
            createNewAdmin.setFont(CustomFont.LoadCustomFont(13));
            createNewAdmin.setBackground(new Color(0x4a75e8));
            createNewAdmin.setForeground(Color.WHITE);
            createNewAdmin.setBounds(0, 460, 190, 30);
        }
        

        // Create 'Log out' button
        log_out = new JButton("Log out");
        log_out.setFont(CustomFont.LoadCustomFont(13));
        log_out.setBackground(new Color(0x4a75e8));
        log_out.setForeground(Color.WHITE);
        log_out.setBounds(0, 500, 190, 30);

        // Add the above panels and buttons to InnerPanel
        innerPanel.add(title);
        innerPanel.add(enterdate);
        innerPanel.add(datefield);
        innerPanel.add(searchButton);
        innerPanel.add(standardRooms);
        innerPanel.add(doubleRooms);
        innerPanel.add(suiteRooms);
        innerPanel.add(deluxeRooms);
        if(ifSuperAdmin){
            innerPanel.add(importButton);
            innerPanel.add(exportButton);
            innerPanel.add(createNewAdmin);
        }
        innerPanel.add(log_out);

        // Add InnerPanel to panel
        this.add(innerPanel);

        // Add action listener to search button
        searchButton.addActionListener(e -> {
            try {
                search();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        // Add action listener to logout button
        log_out.addActionListener(e -> logOut());

        
        if(ifSuperAdmin){
            // Add action listener to importButton
            importButton.addActionListener(e -> importXML());
            
            // Add action listener to exportButton
            exportButton.addActionListener(e -> {
                try {
                    exportXML();
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
            // Add action listener to new admin button
            createNewAdmin.addActionListener(e -> createNewAdmin());
        }

        perLoginAdmin();
    }

    public void perLoginAdmin(){
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        java.sql.Date sqlDateToday = java.sql.Date.valueOf(LocalDate.now());
        datefield.setText(todayString);

        // Set all rooms available and search for room availability
        try {
            CrudAndOthers.setAllRoomsToAvailable(); // REset availability
            search(); // Display availability of the date you searched
            rightPanel.setSearchDate(sqlDateToday); // Update right panel
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    // import button logic
    public void importXML() {
        System.out.println("Pressed import button");

        // Hashmaps for storing old and new IDs
        HashMap<String, String> customerIDs = new HashMap<>();  
        HashMap<String, String> roomIDs = new HashMap<>();
        HashMap<String, String> bookingIDs = new HashMap<>();      

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); 
        
        int result = fileChooser.showSaveDialog(null);

        // if not approve_option
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("Selection cancelled.");
            return; // end method
        }

        // if approve_option
        File selectedFileOrDirectory = fileChooser.getSelectedFile();
        String path = selectedFileOrDirectory.getAbsolutePath();
        System.out.println("Path to XML: " + path);

        try {
            File fXmlFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            System.out.println("----------------------------");

            NodeList customersList = doc.getElementsByTagName("customers");
            if (customersList.getLength() > 0) {
                Node customersNode = customersList.item(0);
                NodeList children = customersNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("customer_id")) {
                        Element customerElement = (Element) child;

                        // Save the old customer ID
                        String oldCustomerID = customerElement.getAttribute("id");
                        System.out.println("Old Customer id: " + oldCustomerID);

                        NodeList customerFields = customerElement.getChildNodes();
                        String lastName = null, firstName = null, contactNum = null, email = null;

                        for (int j = 0; j < customerFields.getLength(); j++) {
                            Node field = customerFields.item(j);
                            if (field.getNodeType() == Node.ELEMENT_NODE) {
                                System.out.println(field.getNodeName() + ": " + field.getTextContent());
                                if(field.getNodeName().equals("last_name")){
                                    lastName = field.getTextContent();
                                } else if(field.getNodeName().equals("first_name")){
                                    firstName = field.getTextContent();
                                } else if(field.getNodeName().equals("contact_num")){
                                    contactNum = field.getTextContent();
                                } else if(field.getNodeName().equals("email")){
                                    email = field.getTextContent();
                                }
                            }
                        }
                        // Save the new customer ID generated from inserting it into the db
                        String newCustomerID = CrudAndOthers.importCustomers(lastName, firstName, contactNum, email);
                        System.out.println("New Customer id: " + newCustomerID);

                        // save both old and new customer IDs inside the hashmap
                        customerIDs.put(oldCustomerID, newCustomerID);
                    }
                }
            }

            NodeList roomsList = doc.getElementsByTagName("rooms");
            if (roomsList.getLength() > 0) {
                Node roomsNode = roomsList.item(0);
                NodeList children = roomsNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("room_id")) {
                        Element roomElement = (Element) child;

                        // Save the old room id 
                        String oldRoomID = roomElement.getAttribute("id");
                        System.out.println("Old Room id: " + oldRoomID);

                        NodeList roomFields = roomElement.getChildNodes();
                        String roomNumber = null, roomType = null, roomStatus = null;
                        for (int j = 0; j < roomFields.getLength(); j++) {
                            Node field = roomFields.item(j);
                            if (field.getNodeType() == Node.ELEMENT_NODE) {
                                System.out.println(field.getNodeName() + ": " + field.getTextContent());
                                if(field.getNodeName().equals("room_number")){
                                    roomNumber = field.getTextContent();
                                } else if(field.getNodeName().equals("type")){
                                    roomType = field.getTextContent();
                                } else if(field.getNodeName().equals("room_status")){
                                    roomStatus = field.getTextContent();
                                }
                            }
                        }
                        // save the new room id generated from inserting into the db
                        String newRoomID = CrudAndOthers.importRooms(roomNumber, roomType, roomStatus);
                        System.out.println("New Room id: " + newRoomID);

                        roomIDs.put(oldRoomID, newRoomID);
                    }
                }
            }

            NodeList bookingsList = doc.getElementsByTagName("bookings");
            if (bookingsList.getLength() > 0) {
                Node bookingsNode = bookingsList.item(0);
                NodeList children = bookingsNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("booking_id")) {
                        Element bookingElement = (Element) child;

                        String oldBookingID = bookingElement.getAttribute("id");
                        System.out.println("Old Booking id: " + oldBookingID);
                        NodeList bookingFields = bookingElement.getChildNodes();
                        String bookingUuid = null, customerId = null, roomId = null, checkInDate = null, checkOutDate = null, bookingDate = null, bookingStatus = null;

                        for (int j = 0; j < bookingFields.getLength(); j++) {
                            Node field = bookingFields.item(j);
                            if (field.getNodeType() == Node.ELEMENT_NODE) {
                                System.out.println(field.getNodeName() + ": " + field.getTextContent());
                                if(field.getNodeName().equals("booking_uuid")){
                                    bookingUuid = field.getTextContent();
                                } else if(field.getNodeName().equals("customer_id")){
                                    customerId = customerIDs.get(field.getTextContent());
                                } else if(field.getNodeName().equals("room_id")){
                                    roomId = roomIDs.get(field.getTextContent());
                                } else if(field.getNodeName().equals("check_in_date")){
                                    checkInDate = field.getTextContent();
                                } else if(field.getNodeName().equals("check_out_date")){
                                    checkOutDate = field.getTextContent();
                                } else if(field.getNodeName().equals("booking_date")){
                                    bookingDate = field.getTextContent();
                                } else if(field.getNodeName().equals("booking_status")){
                                    bookingStatus = field.getTextContent();
                                } 
                            }
                        }
                        String newBookingID = CrudAndOthers.importBooking(bookingUuid, Integer.parseInt(customerId), Integer.parseInt(roomId), checkInDate, checkOutDate, bookingDate, bookingStatus);
                        System.out.println("New Booking id: " + newBookingID);

                        bookingIDs.put(oldBookingID, newBookingID);
                    }
                }
            }

            NodeList paymentsList = doc.getElementsByTagName("payments");
            if (paymentsList.getLength() > 0) {
                Node paymentsNode = paymentsList.item(0);
                NodeList children = paymentsNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("payment_id")) {
                        Element paymentElement = (Element) child;

                        System.out.println("Payment id: " + paymentElement.getAttribute("id"));
                        NodeList paymentFields = paymentElement.getChildNodes();
                        String bookingId = null, amount = null, paymentDate = null, paymentMethod = null;

                        for (int j = 0; j < paymentFields.getLength(); j++) {
                            Node field = paymentFields.item(j);
                            if (field.getNodeType() == Node.ELEMENT_NODE) {
                                System.out.println(field.getNodeName() + ": " + field.getTextContent());
                                if(field.getNodeName().equals("booking_id")){
                                    bookingId = bookingIDs.get(field.getTextContent());
                                } else if(field.getNodeName().equals("amount")){
                                    amount = field.getTextContent();
                                } else if(field.getNodeName().equals("payment_date")){
                                    paymentDate = field.getTextContent();
                                } else if(field.getNodeName().equals("payment_method")){
                                    paymentMethod = field.getTextContent();
                                } 
                            }
                        }
                        CrudAndOthers.importPayments(Integer.parseInt(bookingId), Double.parseDouble(amount), paymentDate, paymentMethod);
                       
                    }
                }
            }

            NodeList staffList = doc.getElementsByTagName("staff");
            if (staffList.getLength() > 0) {
                Node staffNode = staffList.item(0);
                NodeList children = staffNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("staff_id")) {
                        Element staffElement = (Element) child;
                        System.out.println("Staff id: " + staffElement.getAttribute("id"));
                        String lastName = null, firstName = null, password = null, email = null;

                        NodeList staffFields = staffElement.getChildNodes();
                        for (int j = 0; j < staffFields.getLength(); j++) {
                            Node field = staffFields.item(j);
                            if (field.getNodeType() == Node.ELEMENT_NODE) {
                                System.out.println(field.getNodeName() + ": " + field.getTextContent());
                                if(field.getNodeName().equals("last_name")){
                                    lastName = field.getTextContent();
                                } else if(field.getNodeName().equals("first_name")){
                                    firstName = field.getTextContent();
                                } else if(field.getNodeName().equals("password")){
                                    password = field.getTextContent();
                                } else if(field.getNodeName().equals("email")){
                                    email = field.getTextContent();
                                }
                            }
                        }
                        CrudAndOthers.importStaff(lastName, firstName, password, email);
                        System.out.println();
                    }
                }
            } 

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred during the importing process.", "Error During Importing", JOptionPane.ERROR_MESSAGE);
        }

    }

    public int getCount(String tag, String element, Document doc){
        NodeList list = doc.getElementsByTagName(tag);
        int count = 0;
        if (list.getLength() > 0) {
            Node node = list.item(0); // The <bookings> node
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(element)) {
                    count++;
                }
            }
            System.out.println("Number of elements under <" + tag +">: " + count);
            return count;
        }
        return 0;
    }

    // export button logic
    public void exportXML() throws SQLException {
        boolean successfullyExportedData = true;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        int result = fileChooser.showSaveDialog(null);
        String xmlExportDirectory;

        // if not approve_option
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("Selection cancelled.");
            return;
        }

        // if approve_option
        File selectedFileOrDirectory = fileChooser.getSelectedFile();
        String path = selectedFileOrDirectory.getAbsolutePath();
        xmlExportDirectory = path;

        System.out.println("Selected Path: " + xmlExportDirectory);

        File exportDirectory = new File(xmlExportDirectory);
        exportDirectory.mkdir(); // creates a directory/folder

        // This will be useful for later lol
        ArrayList<String[]> elementsArrayList = new ArrayList<>();
        elementsArrayList.add(new String[] {"bookings", "booking_id", "booking_uuid", "customer_id", "room_id", "check_in_date", "check_out_date", "booking_date", "booking_status"});
        elementsArrayList.add(new String[] {"customers", "customer_id", "last_name", "first_name", "contact_num", "email"});
        elementsArrayList.add(new String[] {"payments", "payment_id", "booking_id", "amount", "payment_date", "payment_method"});
        //elementsArrayList.add(new String[] {"room_types", "types", "capacity", "price"});
        elementsArrayList.add(new String[] {"rooms", "room_id", "room_number", "type", "room_status"});
        elementsArrayList.add(new String[] {"staff", "staff_id", "last_name", "first_name", "password", "email"});

        // This will also be useful later on lol
        ArrayList<ArrayList<String[]>> dataFromDB = new ArrayList<>();
        for(String[] elements : elementsArrayList) {
            dataFromDB.add(CrudAndOthers.getAllTableData(elements));
        }

        // Gets the current date and uses it as the file version info (to allow for different version types)
        LocalDateTime today = LocalDateTime.now();
        String date = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String hour = (today.getHour() < 10) ? "0": "" + Integer.toString(today.getHour());
        String min = (today.getMinute() < 10) ? "0": "" + Integer.toString(today.getMinute());
        String sec = (today.getSecond() < 10) ? "0": "" + Integer.toString(today.getSecond());
        String fileName = "Database Ver. " + date + " " + hour + min + sec;

        successfullyExportedData = exportTableToXML(fileName, elementsArrayList, dataFromDB, xmlExportDirectory);

        if(successfullyExportedData)
            JOptionPane.showMessageDialog(this, "Data successfully exported (XML) in the selected directory", "Successfully Exported Data", JOptionPane.INFORMATION_MESSAGE);
        else 
            JOptionPane.showMessageDialog(this, "An error occurred during the exporting process.", "Error During Exporting", JOptionPane.ERROR_MESSAGE);
        
    }

    public boolean exportTableToXML(String fileName, ArrayList<String[]> elementsArrayList, ArrayList<ArrayList<String[]>> dataContentArrayList, String xmlExportDirectory) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        
        try {
            builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            
            // create the root of the document
            Element root = document.createElement("hotelbookingmanagement");
            
            for(int i = 0; i < elementsArrayList.size(); i++){
                System.out.println(i);
                String[] elements = elementsArrayList.get(i); // get the set of elements from the arraylist
                ArrayList<String[]> dataContent = dataContentArrayList.get(i); // get the arraylist of elements from the double array
                
                // create an element just above root that contains the table name as a tag
                Element table = document.createElement(elements[0]);
                System.out.println("size of dataContent = " + dataContent.size());
                System.out.println("length of elements = " + elements.length);
                
                for(int j = 0; j < dataContent.size(); j++){        
                    String[] stringArray = dataContent.get(j); //we get a new string array at index j
                    
                    // create an id tag for each data 
                    Element id = document.createElement(elements[1]);

                    // make tag as id if not "room_types"
                    //if(!elements[0].equals("room_types")) 
                    id.setAttribute("id", stringArray[0]);
                    //else id.setAttribute("category", stringArray[0]);
                
                    // start at the 2nd iteration since 0th iteration (elements) is name of table and the 1th iteration is id
                    for(int k = 2; k < elements.length; k++){
                        Element element = document.createElement(elements[k]);
                        element.setTextContent(stringArray[k-1]);
                        id.appendChild(element);
                    }
                    // append the id element to the table element
                    table.appendChild(id);
                }
                // append the table element into the root element
                root.appendChild(table);
            }
            // finally, append the root to the document 
            document.appendChild(root);

            // exporting the entire XML document to an XML file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource src = new DOMSource(document);
            StreamResult result = new StreamResult(xmlExportDirectory + "/" + fileName + ".xml");
            transformer.transform(src, result);
            System.out.println("XML File created successfully!");
            return true;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } 
        return false;
    }

    // Logout button logic
    public void logOut() {
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Log Out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            datefield.setText("");
            standardRooms.update(0, 0);
            doubleRooms.update(0, 0);
            suiteRooms.update(0, 0);
            deluxeRooms.update(0, 0);
            cardLayout.show(cardPanel, "Main Page");
        }
    }

    // Search button logic
    public void search() throws SQLException {
        try {
            // Parse date input
            String dateString = datefield.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateSearchConvert = LocalDate.parse(dateString, formatter);
            java.sql.Date dateSearch = java.sql.Date.valueOf(dateSearchConvert);
            System.out.println(dateString);

            // Reset and update room status
            CrudAndOthers.setAllRoomsToAvailable();
            CrudAndOthers.setRoomsToUnavailable(dateSearch);
            rightPanel.setSearchDate(dateSearch);

            // Get room availability and total count for standard
            int standardTotal = CrudAndOthers.getAvailable("standard", dateSearch);
            int standardTotalRooms = CrudAndOthers.getTotalRooms("standard");
            standardRooms.update(standardTotal, standardTotalRooms);
            System.out.println(standardTotal);
            System.out.println(standardTotalRooms);

            // Get room availability and total count for double
            int doubleTotal = CrudAndOthers.getAvailable("double", dateSearch);
            int doubleTotalRooms = CrudAndOthers.getTotalRooms("double");
            doubleRooms.update(doubleTotal, doubleTotalRooms);
            System.out.println(doubleTotal);
            System.out.println(doubleTotalRooms);

            // Get room availability and total count for suite
            int suiteTotal = CrudAndOthers.getAvailable("suite", dateSearch);
            int suiteTotalRooms = CrudAndOthers.getTotalRooms("suite");
            suiteRooms.update(suiteTotal, suiteTotalRooms);
            System.out.println(suiteTotal);
            System.out.println(suiteTotalRooms);

            // Get room availability and total count for deluex
            int deluxeTotal = CrudAndOthers.getAvailable("deluxe", dateSearch);
            int deluxeTotalRooms = CrudAndOthers.getTotalRooms("deluxe");
            deluxeRooms.update(deluxeTotal, deluxeTotalRooms);
            System.out.println(deluxeTotal);
            System.out.println(deluxeTotalRooms);

            // Refresh rightpanel display
            repaint();
            rightPanel.refreshRooms();
            rightPanel.revalidate();
            rightPanel.repaint();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Invalid date format. Please enter the date in yyyy-MM-dd format.");
        }
    }

    // Switch to create new admin panel
    public void createNewAdmin() {
        cardLayout.show(cardPanel, "Create New Admin");
    }
}
