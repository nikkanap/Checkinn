This is a public copy of the `Checkinn Hotem Booking Management System` Developed as a final project for our CMSC 127 Subject.

Disclaimer: This repository is for resume purposes. Moreover, I do not take full credit for the full project. I simply helped to contribute. 

-----

CheckInn | Hotel Booking Management System

Welcome to CheckInn, your all-in-one solution for managing hotel bookings, room availability, and staff access with ease.

System Requirements
Before using CheckInn, ensure your system meets the following requirements.

Hardware Requirements
- Processor: 1 GHz or higher
- RAM: 4 GB minimum
- Storage: 100 MB available space

Software Requirements
- Java Standard  Edition (JSE) Version 8 or higher. Download: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
- Java Development Kit (JDK) Version 24 or higher. Download: https://www.oracle.com/ph/java/technologies/downloads/
- MySQL Server. Version 8 or higher. Download: https://dev.mysql.com/downloads/

Installation Instructions
1. Follow these steps to install and run the CheckInn system.
2. Install JSE 8 or higher
3. Install JDK 24 or higher
4. Install and start MySQL Server 8.0.40 (Note: Using other applications like XAMPP is not recommended as compatibility with the application has not been tested. For better results, use the standalone MySQL Server)
5. Download and unzip the compiled CMSC 127 Hotel Booking Management System folder of the CheckInn application.
6. Double click the .jar file to run the application.

Database Connection
1. When you open the application, a splash screen will appear prompting you to connect to your MySQL database, enter your MySQL password.
2. Click Test Connection to verify your credentials.
3. If successful, click Save to store the connection. This allows the application to connect to the database securely without modifying environment variables manually.

Booking a Room (User)
1. Once connected to the database, you’ll land on the Home Screen.
2. Click the Book Now button to go to the booking page. 
3. Select your desired check-in and check-out dates.
4. To change the dates, click the Reset button
5. Click Check Availability to view available rooms.
6. Once you find a suitable room, click Book Now.
7. Fill in your booking details and confirm. You will receive a Booking ID – please present this at the front desk during check-in.

Staff Login
1. On the Home Screen, click the Admin button.
2. Enter your credentials to access the Staff Dashboard.

Creating a New Admin (Super Admin Only)
1. To add a new admin, Log in as super admin (Super Admin credentials are given on User Manual).
2. In the Staff Dashboard, click Create New Admin.
3. Fill out the required information and click Submit.

Checking Room Availability (Staff Only)
1. Log in as staff.
2. On the dashboard, enter a date and click Search.
3. View the room summary on the left panel.

Creation and Use of Backup Files
1. To create a backup file of your current database, you must be logged in to the application's key email address (Super Admin credentials are given on User Manual).
2. To import the current database into a backup file, click the import button found on the lower left side of the screen.
3. A pop-up will appear allowing you to choose the desired location for your backup file. Once you have chosen a location, click save. 
4. To export from a backup file, click the Export button located in the lower left corner of the screen. 
5. A pop-up will appear. Locate the destination of your backup file. Select the file, then click save.
