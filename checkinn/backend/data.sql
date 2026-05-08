USE `hotelbookingmanagement`;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
INSERT INTO `staff` VALUES (1,'Reyes','Miguel','mreyes2025','staffReyes@gmail.com'),(2,'Santos','Anna','asantos!pwd','staffSantos@gmail.com'),(3,'Garcia','Luis','lgarcia123','staffGarcia@gmail.com'),(4,'Torres','Carla','ctpass456','staffTorres@gmail.com'),(5,'Morales','Jake','jm_secure','staffMorales@gmail.com'),(6,'Alvarico','Mac','BossChinaDoll','staffAlvarico@gmail.com'),(7, 'Admin', 'Super', 'SuperAdmin123', 'superAdmin@gmail.com');
UNLOCK TABLES;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
INSERT INTO `customers` VALUES (101,'Dela Cruz','Louis','12345678910','dlouis@gmail.com'),(102,'Santos','Maria','12345678911','msantos@gmail.com'),(103,'Reyes','Carlos','12345678912','creyes@gmail.com'),(104,'Garcia','Angela','12345678913','agarcia@gmail.com'),(105,'Torres','Miguel','12345678914','mtorres@gmail.com');
UNLOCK TABLES;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
INSERT INTO `rooms` VALUES
(1,'101A','standard','available'),
(2,'102B','double','available'),
(3,'201C','suite','unavailable'),
(4,'202D','suite','available'),
(5,'301E','deluxe','available'),
(6,'103F','standard','available'),
(7,'104G','double','unavailable'),
(8,'203H','suite','available'),
(9,'204I','suite','available'),
(10,'302J','deluxe','unavailable'),
(11,'105K','standard','available'),
(12,'106L','double','available'),
(13,'205M','suite','unavailable'),
(14,'206N','suite','available'),
(15,'303O','deluxe','available'),
(16,'107P','standard','unavailable'),
(17,'108Q','double','available'),
(18,'207R','suite','available'),
(19,'208S','suite','unavailable'),
(20,'304T','deluxe','available'),
(21,'109U','standard','available'),
(22,'110V','double','unavailable'),
(23,'209W','suite','available'),
(24,'210X','suite','available'),
(25,'305Y','deluxe','unavailable');
UNLOCK TABLES;

--
-- Dumping data for table `room_types`
--

LOCK TABLES `room_types` WRITE;
INSERT INTO `room_types` VALUES ('standard',2,1500),('double',2,2500),('suite',6,3600),('deluxe',4,2900);
UNLOCK TABLES;

LOCK TABLES `bookings_backup` WRITE;
/*!40000 ALTER TABLE `bookings_backup` DISABLE KEYS */;
INSERT INTO `bookings_backup` VALUES (11,101,1,'2025-05-10','2025-05-12','2025-05-03 01:17:36','confirmed'),(12,102,2,'2025-06-01','2025-06-05','2025-05-03 01:17:36','confirmed'),(13,103,3,'2025-07-15','2025-07-20','2025-05-03 01:17:36','confirmed'),(14,104,4,'2025-05-08','2025-05-10','2025-05-03 01:17:36','confirmed'),(15,105,5,'2025-08-01','2025-08-03','2025-05-03 01:17:36','confirmed'),(16,106,2,'2025-05-16','2025-05-24','2025-05-14 17:40:12','confirmed');
/*!40000 ALTER TABLE `bookings_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;

INSERT INTO `bookings` VALUES (11,'uuidsample1', 101,1,'2025-05-10','2025-05-12','2025-05-03 09:17:36','confirmed'),(12, 'uuidsample2', 102,2,'2025-06-01','2025-06-05','2025-05-03 09:17:36','confirmed'),(13, 'uuidsample3',103,3,'2025-07-15','2025-07-20','2025-05-03 09:17:36','confirmed'),(14,'uuidsample4',104,4,'2025-05-08','2025-05-10','2025-05-03 09:17:36','confirmed'),(15,'uuidsample5',105,5,'2025-08-01','2025-08-03','2025-05-03 09:17:36','confirmed');

UNLOCK TABLES;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;

INSERT INTO `payments` VALUES (6,11,2500.00,'2025-05-10 02:15:00','card'),(7,12,3200.00,'2025-06-01 01:00:00','cash'),(8,13,4500.00,'2025-07-15 06:30:00','gcash'),(9,14,6000.00,'2025-05-08 08:45:00','card'),(10,15,3000.00,'2025-08-01 03:20:00','cash');

UNLOCK TABLES;
