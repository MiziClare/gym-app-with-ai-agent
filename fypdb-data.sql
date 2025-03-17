-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: fyp_fitness
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'username',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'password',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'name',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'avatar',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'role',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'phone',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'email',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='admin';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin0','123','admin0','http://localhost:9090/files/avatar_admin0.png','ADMIN','0123456789','admin0@example.com');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_group`
--

DROP TABLE IF EXISTS `chat_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_group` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `chat_user_id` int DEFAULT NULL COMMENT 'chat user ID',
  `user_id` int DEFAULT NULL COMMENT 'user ID',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'role',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='chat group table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_group`
--

LOCK TABLES `chat_group` WRITE;
/*!40000 ALTER TABLE `chat_group` DISABLE KEYS */;
INSERT INTO `chat_group` VALUES (44,1,2,'USER'),(45,2,1,'COACH'),(46,1,6,'USER'),(47,6,1,'COACH'),(48,2,6,'USER'),(49,6,2,'COACH'),(50,1,1,'USER'),(51,1,1,'COACH'),(52,3,6,'USER'),(53,6,3,'COACH'),(54,6,6,'USER'),(55,6,6,'COACH'),(56,2,5,'USER'),(57,5,2,'COACH'),(58,4,5,'USER'),(59,5,4,'COACH'),(60,1,5,'USER'),(61,5,1,'COACH'),(62,2,7,'USER'),(63,7,2,'COACH'),(64,1,7,'USER'),(65,7,1,'COACH'),(66,3,7,'USER'),(67,7,3,'COACH'),(68,4,7,'USER'),(69,7,4,'COACH'),(70,6,7,'USER'),(71,7,6,'COACH');
/*!40000 ALTER TABLE `chat_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_info`
--

DROP TABLE IF EXISTS `chat_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `chat_user_id` int DEFAULT NULL COMMENT 'chat user ID',
  `user_id` int DEFAULT NULL COMMENT 'user ID',
  `text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'chat text',
  `isread` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Unread' COMMENT 'is read',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'text time',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'role',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='chat info table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_info`
--

LOCK TABLES `chat_info` WRITE;
/*!40000 ALTER TABLE `chat_info` DISABLE KEYS */;
INSERT INTO `chat_info` VALUES (44,1,6,'Hi, Mr Leon!','Unread','2025-03-17 15:25:39','USER'),(45,2,6,'Hi, Ada?','Read','2025-03-17 15:28:38','USER'),(46,2,5,'Hi, Ada! This is Sam. I want to book a private class Tuesday 16:00','Read','2025-03-17 15:46:20','USER'),(47,4,5,'Hi, Miss Claire!','Read','2025-03-17 15:46:45','USER'),(48,1,5,'Hi!','Unread','2025-03-17 15:47:18','USER'),(49,5,2,'np\n','Unread','2025-03-17 15:48:53','USER'),(50,5,2,'See u then','Unread','2025-03-17 15:51:28','USER'),(51,5,2,'O(∩_∩)O','Unread','2025-03-17 15:52:49','USER'),(52,5,2,'Hi','Unread','2025-03-17 15:58:22','USER'),(53,2,7,'Hi, Ada! This is Geralt.\n','Read','2025-03-17 16:06:22','USER'),(54,7,2,'Hi!','Read','2025-03-17 16:06:51','USER'),(55,7,2,'u wanne try a private session?','Read','2025-03-17 16:11:10','USER'),(56,7,2,'?','Read','2025-03-17 16:12:32','USER'),(57,6,2,'Hi! Nice to meet u!','Read','2025-03-17 16:23:41','USER'),(58,6,2,'?','Read','2025-03-17 16:24:05','USER'),(59,7,2,'❀\n','Read','2025-03-17 16:28:07','USER'),(60,7,2,'Hi~ o(*￣▽￣*)ブ','Read','2025-03-17 16:31:07','COACH'),(61,3,6,'Hi!','Read','2025-03-17 16:31:49','USER'),(62,6,3,'Hi! how r u','Read','2025-03-17 16:32:34','COACH'),(63,6,6,'Hi!','Read','2025-03-17 16:47:43','USER'),(64,1,7,'Hi!','Unread','2025-03-17 18:28:01','USER'),(65,3,7,'Hi!','Unread','2025-03-17 18:28:07','USER');
/*!40000 ALTER TABLE `chat_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coach`
--

DROP TABLE IF EXISTS `coach`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coach` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'username',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'password',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'name',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'role',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'avatar',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'bio',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'phone',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'email',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='coach info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coach`
--

LOCK TABLES `coach` WRITE;
/*!40000 ALTER TABLE `coach` DISABLE KEYS */;
INSERT INTO `coach` VALUES (1,'coach0','123','Leon','COACH','http://localhost:9090/files/1741041831663-avatar-coach.png','<p><strong>? Leon Kaid– Your Elite Fitness Coach ?</strong></p><p>Hey there! I’m <strong>Leon</strong>, a fitness coach with a passion for strength, endurance, and tactical training. You may know me for surviving high-stakes situations, but my real mission is to help YOU conquer your fitness goals with the same level of discipline and resilience.</p><p><img src=\"http://localhost:9090/files/1741041085702-bio-leon.jpg\" contenteditable=\"false\" width=\"50%\"/></p><p>? <strong>My Training Philosophy:</strong><br/>✔ <strong>Strength &amp; Endurance</strong> – Build a body that’s both powerful and agile.<br/>✔ <strong>Survival Fitness</strong> – Functional workouts to enhance speed, reaction time, and stamina.<br/>✔ <strong>Mental Toughness</strong> – True strength comes from both body and mind.</p><p>? Whether you’re looking to bulk up, increase stamina, or train like an elite operative, I’m here to push you past your limits. Let’s train hard, stay sharp, and become unstoppable together.</p><p>? <strong>\"Stay strong. Stay fast. Stay ready.\"</strong> ?</p>','0123456789','coach0@example.com'),(2,'coach1','123','Ada','COACH','http://localhost:9090/files/1741041839242-avatar-coach.png','<p><strong>? Ada King– Elite Fitness Coach ?</strong></p><p>? <strong>Ex-Spy Turned Fitness Guru | Strength &amp; Agility Expert | Mind &amp; Body Precision</strong> ?️‍♀️</p><p>Hey there, fitness warriors! I’m <strong>Ada</strong>, your ultimate guide to strength, speed, and endurance. Once navigating high-stakes missions, now I help YOU conquer your own fitness challenges! ?</p><p><img src=\"http://localhost:9090/files/1741041412782-bio-ada.jpg\" contenteditable=\"false\" width=\"50%\"/><br/></p><p>? <strong>Specialties:</strong><br/>?‍♀️ <strong>Agility &amp; Speed Training</strong> – Move like a shadow, strike like thunder ⚡<br/>?️‍♀️ <strong>Strength &amp; Conditioning</strong> – Power that’s not just for show ?<br/>?‍♀️ <strong>Flexibility &amp; Core Control</strong> – Because balance is everything ?<br/>? <strong>Combat Fitness</strong> – Self-defense meets total-body fitness ?‍♀️</p><p>? <strong>My Approach:</strong><br/>✔ <strong>Stealth &amp; Strength</strong> – Train smart, train strong, train sleek ?<br/>✔ <strong>No Excuses, Only Results</strong> – Your body is your best weapon ?<br/>✔ <strong>Confidence &amp; Control</strong> – Fitness isn’t just physical, it’s mental too ?</p><p>Ready to unlock your ultimate form? Let’s train like a pro and move like a ghost. ?</p>','0123456789','coach1@example.com'),(3,'coach2','123','Jill','COACH','http://localhost:9090/files/1741041848532-avatar-coach.png','','0123456789','coach2@example.com'),(4,'coach3','123','Claire','COACH','http://localhost:9090/files/1741041857661-avatar-coach.png','','0123456789','coach3@example.com'),(5,'coach4','123','Ethan','COACH','http://localhost:9090/files/1741041867291-avatar-coach.png','','0123456789','coach4@example.com'),(6,'coach5','123','Chris','COACH','http://localhost:9090/files/1741041875831-avatar-coach.png','','0123456789','coach5@exmaple.com');
/*!40000 ALTER TABLE `coach` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT 'Commenter',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Comment Content',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Comment Time',
  `coach_id` int DEFAULT NULL COMMENT 'Coach ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='comment section of coach';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,5,'Very responsible coach, trustworthy','2025-02-27 22:59:53',1),(2,6,'Great skills and helped me a lot??!','2025-02-27 23:08:06',1),(3,NULL,'Nice coach!','2025-02-28 08:46:38',2),(4,NULL,'Good','2025-02-28 09:20:59',5),(5,5,'Great!','2025-02-28 09:46:26',2),(6,6,'Really nice!','2025-03-03 21:54:01',3),(7,7,'The best!','2025-03-03 22:52:10',1),(8,7,'❤','2025-03-03 22:52:41',3),(9,6,'Very patient','2025-03-09 11:06:23',2);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'course image',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'course name',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'course duration',
  `price` double(10,2) DEFAULT NULL COMMENT 'course price',
  `coach_id` int DEFAULT NULL COMMENT 'coach ID of the course',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'course intro',
  `total_sessions` int NOT NULL DEFAULT '1' COMMENT 'total sessions of the course',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='course info table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (11,'http://localhost:9090/files/1740708725080-course-swim.jpg','Swimming Class','60 min',15.00,1,'<p>Dive into fitness with our expert-led swimming classes! Whether you\'re a beginner learning the basics or an advanced swimmer refining your strokes, our state-of-the-art pool and professional instructors ensure a safe and engaging experience. Improve your technique, boost endurance, and enjoy the benefits of a full-body workout in the water!</p><img src=\"http://localhost:9090/files/1740708879291-course-swim.jpg\" contenteditable=\"false\"/>',10),(12,'http://localhost:9090/files/1740708822403-course-core.jpg','Core Training','45 min',12.00,1,'<p>Strengthen your core like never before! This high-energy class focuses on engaging your abdominal, lower back, and oblique muscles with targeted exercises such as planks, crunches, and resistance training. Designed to enhance your stability, balance, and posture, this class is perfect for all fitness levels looking to sculpt a stronger core!</p><p><img src=\"http://localhost:9090/files/1740708864707-course-core.jpg\" contenteditable=\"false\"/></p>',3),(13,'http://localhost:9090/files/1740708912889-course-pilates.jpg','Pilates Class','50 min',14.00,2,'<p>Discover the power of controlled movement with Pilates! This class combines precise, low-impact exercises that improve flexibility, build core strength, and enhance overall body awareness. Guided by experienced instructors, you’ll feel stronger, more aligned, and revitalized after each session. Perfect for those looking to tone their muscles while promoting a sense of inner balance.</p><img src=\"http://localhost:9090/files/1740708944968-course-pilates.jpg\" contenteditable=\"false\"/>',6),(14,'http://localhost:9090/files/1740709058448-course-yoga.jpg','Yoga Class','60 min',13.00,2,'<p>Breathe, stretch, and restore your mind and body in our Yoga class. From gentle Hatha yoga to dynamic Vinyasa flows, this session helps improve flexibility, strength, and mindfulness. Experience deep relaxation and enhanced well-being through guided poses, breathwork, and meditation techniques. Suitable for all levels!</p><img src=\"http://localhost:9090/files/1740709094527-course-yoga.jpg\" contenteditable=\"false\"/>',5),(15,'http://localhost:9090/files/1740709643887-course-treadmill.jpg','Treadmill Class','45 min	',0.00,3,'<p>Elevate your cardio routine with our high-energy treadmill classes! This class combines interval training, hill runs, and endurance challenges to help you burn calories and improve cardiovascular health. Set to motivating music and guided by expert trainers, you\'ll push your limits and leave feeling accomplished!</p><img src=\"http://localhost:9090/files/1740709658360-course-treadmill.jpg\" contenteditable=\"false\"/>',1),(16,'http://localhost:9090/files/1740709716105-course-kickboxing.jpg','Body Combat','50 min',14.00,6,'<p>Unleash your power in our Body Combat class! This adrenaline-pumping workout blends martial arts, kickboxing, and cardio drills to improve strength, endurance, and agility. Punch, kick, and strike your way to fitness while burning calories and having fun! No prior experience is needed—just bring your energy and determination!</p><img src=\"http://localhost:9090/files/1740709724342-course-kickboxing.jpg\" contenteditable=\"false\"/>',6),(17,'http://localhost:9090/files/1740709753362-course-spin.jpg','Spin Class','45 min',12.00,4,'<p>Get ready to ride to the rhythm! Our Spin class is a high-intensity, music-driven indoor cycling experience that will challenge your endurance, build leg strength, and torch calories. With resistance variations and simulated hill climbs, this class keeps you motivated from start to finish. Perfect for all fitness levels!</p><img src=\"http://localhost:9090/files/1740709783001-course-spin.jpg\" contenteditable=\"false\"/>',1),(18,'http://localhost:9090/files/1740709822766-course-bodypump.jpg','Body Pump','50 min	',14.00,5,'<p>Sculpt and tone your entire body with our high-rep, low-weight Body Pump class! This strength-based workout uses barbells and resistance training to target all major muscle groups. Led by expert instructors and set to powerful music, you\'ll leave feeling stronger and more confident after every session!</p><img src=\"http://localhost:9090/files/1740709870386-course-bodypump.jpg\" contenteditable=\"false\"/>',3),(19,'http://localhost:9090/files/1740709892418-course-functional.jpg','Functional Training','45 min	',13.00,4,'<p>Train like an athlete with our Functional Training class! Designed to enhance everyday movements, this workout incorporates kettlebells, battle ropes, TRX, and bodyweight exercises to improve strength, coordination, and agility. Whether you\'re an athlete or a beginner, this class prepares you for real-life physical challenges!</p><img src=\"http://localhost:9090/files/1740709927621-course-functional.jpg\" contenteditable=\"false\"/>',5),(20,'http://localhost:9090/files/1740709946460-course-stretch.jpg','Stretch & Relax','40 min',11.00,5,'<p>Release tension and enhance flexibility in our Stretch &amp; Relax class! This session combines deep stretching, mobility exercises, and breathwork to improve muscle recovery, reduce stress, and promote overall well-being. A perfect complement to any workout routine, this class will leave you feeling refreshed and revitalized.</p><img src=\"http://localhost:9090/files/1740709970118-course-stretch.jpg\" contenteditable=\"false\"/>',2),(21,'http://localhost:9090/files/1740710000074-course-childswim.jpg','Children’s Swimming','45 min',10.00,5,'<p>Give your child the confidence to swim with our engaging and fun Children’s Swimming class! Designed for kids of all skill levels, this program focuses on water safety, technique, and building confidence in the pool. Led by certified instructors in a supportive environment, this class ensures a fun and safe learning experience.</p><img src=\"http://localhost:9090/files/1740710025375-course-childswim.jpg\" contenteditable=\"false\"/>',9),(22,'http://localhost:9090/files/1740710047335-course-meditation.jpg','Meditation Class','30 min',9.00,6,'<p>Find inner peace and clarity in our Meditation class. Through guided mindfulness practices, breathing techniques, and relaxation exercises, this session helps reduce stress, improve focus, and cultivate a sense of calm. Whether you\'re a beginner or experienced meditator, this class provides a serene escape from daily stress.</p><img src=\"http://localhost:9090/files/1740710085621-course-meditation.jpg\" contenteditable=\"false\"/>',3);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_schedule`
--

DROP TABLE IF EXISTS `course_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_schedule` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `course_id` int NOT NULL COMMENT 'course id',
  `weekday` enum('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') NOT NULL COMMENT 'week day',
  `start_time` time NOT NULL COMMENT 'start time',
  `room` varchar(255) NOT NULL DEFAULT 'Room 01' COMMENT 'room',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  CONSTRAINT `fk_course_schedule_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='course schedule table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_schedule`
--

LOCK TABLES `course_schedule` WRITE;
/*!40000 ALTER TABLE `course_schedule` DISABLE KEYS */;
INSERT INTO `course_schedule` VALUES (1,22,'Monday','20:00:00','Room 01'),(2,22,'Wednesday','20:00:00','Room 01'),(3,22,'Friday','20:00:00','Room 01'),(5,21,'Sunday','18:00:00','Pool 02'),(6,21,'Saturday','18:00:00','Pool 02'),(7,20,'Monday','12:50:00','Room 02'),(11,11,'Monday','20:00:00','Pool 01'),(12,11,'Wednesday','20:00:00','Pool 01'),(13,11,'Saturday','20:00:00','Pool 01'),(14,12,'Thursday','14:00:00','Room 03'),(15,19,'Wednesday','09:00:00','Room 02'),(16,18,'Friday','19:00:00','Room 01'),(17,17,'Thursday','18:00:00','Room 03'),(18,17,'Tuesday','18:00:00','Room 03'),(19,16,'Friday','10:00:00','Room 01'),(20,15,'Saturday','15:00:00','Room 02'),(21,14,'Wednesday','13:20:00','Room 01'),(22,14,'Tuesday','13:20:00','Room 01'),(23,13,'Thursday','15:00:00','Room 01'),(24,13,'Wednesday','15:00:00','Room 01');
/*!40000 ALTER TABLE `course_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eqreserve`
--

DROP TABLE IF EXISTS `eqreserve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eqreserve` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `equipment_id` int DEFAULT NULL COMMENT 'equipment ID',
  `user_id` int DEFAULT NULL COMMENT 'user ID',
  `start` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'start time',
  `end` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'end time',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'status of the review',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='equipment reservation info table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eqreserve`
--

LOCK TABLES `eqreserve` WRITE;
/*!40000 ALTER TABLE `eqreserve` DISABLE KEYS */;
INSERT INTO `eqreserve` VALUES (1,20,5,'2025-03-31 11:50:00','2025-03-31 13:00:00','Approved'),(2,27,5,'2026-04-30 21:00:00','2025-04-30 22:00:00','Approved'),(3,12,7,'2025-03-17 00:00:00','2025-03-17 03:00:00','Pending'),(4,4,6,'2025-03-19 00:00:00','2025-03-19 03:00:00','Approved');
/*!40000 ALTER TABLE `eqreserve` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipment`
--

DROP TABLE IF EXISTS `equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'equipment image',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'equipment name',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'equipment position',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'equipment code',
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'equipment instructions',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'equipment status',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='equipment info table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipment`
--

LOCK TABLES `equipment` WRITE;
/*!40000 ALTER TABLE `equipment` DISABLE KEYS */;
INSERT INTO `equipment` VALUES (1,'http://localhost:9090/files/1740837847666-Treadmill.jpg','Treadmill 01','A','A001','Start with a warm-up walk before increasing speed.\nMaintain an upright posture and avoid holding onto the handrails unless necessary.\nAdjust the incline to simulate outdoor running and reduce joint impact.\nWear proper running shoes to minimize the risk of injuries.\nStop gradually and use the emergency stop button in case of an emergency.','? In Use'),(2,'http://localhost:9090/files/1740838638012-Treadmill.jpg','Treadmill 02','A','A002','Start with a warm-up walk before increasing speed.\nMaintain an upright posture and avoid holding onto the handrails unless necessary.\nAdjust the incline to simulate outdoor running and reduce joint impact.\nWear proper running shoes to minimize the risk of injuries.\nStop gradually and use the emergency stop button in case of an emergency.','✅ Available'),(3,'http://localhost:9090/files/1740838682325-Treadmill.jpg','Treadmill 03','A','A003','Start with a warm-up walk before increasing speed.\nMaintain an upright posture and avoid holding onto the handrails unless necessary.\nAdjust the incline to simulate outdoor running and reduce joint impact.\nWear proper running shoes to minimize the risk of injuries.\nStop gradually and use the emergency stop button in case of an emergency.','✅ Available'),(4,'http://localhost:9090/files/1740838695479-Treadmill.jpg','Treadmill 04','A','A004','Start with a warm-up walk before increasing speed.\nMaintain an upright posture and avoid holding onto the handrails unless necessary.\nAdjust the incline to simulate outdoor running and reduce joint impact.\nWear proper running shoes to minimize the risk of injuries.\nStop gradually and use the emergency stop button in case of an emergency.','✅ Available'),(5,'http://localhost:9090/files/1740839064139-Elliptical Trainer.jpg','Elliptical Trainer 01','B','B001','Keep your back straight and engage your core throughout the workout.\nUse both handles for a full-body workout or let go to challenge balance.\nAdjust resistance levels to match your fitness level.\nAvoid excessive forward leaning, as it can strain your lower back.\nMaintain a steady pace instead of sudden, jerky movements.','✅ Available'),(6,'http://localhost:9090/files/1740839118380-Elliptical Trainer.jpg','Elliptical Trainer 02','B','B001','Keep your back straight and engage your core throughout the workout.\nUse both handles for a full-body workout or let go to challenge balance.\nAdjust resistance levels to match your fitness level.\nAvoid excessive forward leaning, as it can strain your lower back.\nMaintain a steady pace instead of sudden, jerky movements.','✅ Available'),(7,'http://localhost:9090/files/1740839774615-Spin Bike.jpg','Spin Bike 01','C','C001','Adjust the seat height so your knees are slightly bent at the lowest pedal position.\nMaintain a firm grip on the handlebars but avoid excessive pressure on your wrists.\nEngage your core and avoid excessive forward leaning.\nIncrease resistance gradually instead of pedaling too fast with low resistance.\nStay hydrated and stretch after the session to prevent stiffness.','✅ Available'),(8,'http://localhost:9090/files/1740839810908-Spin Bike.jpg','Spin Bike 02','C','C002','Adjust the seat height so your knees are slightly bent at the lowest pedal position.\nMaintain a firm grip on the handlebars but avoid excessive pressure on your wrists.\nEngage your core and avoid excessive forward leaning.\nIncrease resistance gradually instead of pedaling too fast with low resistance.\nStay hydrated and stretch after the session to prevent stiffness.','✅ Available'),(9,'http://localhost:9090/files/1740839826250-Spin Bike.jpg','Spin Bike 03','C','C003','Adjust the seat height so your knees are slightly bent at the lowest pedal position.\nMaintain a firm grip on the handlebars but avoid excessive pressure on your wrists.\nEngage your core and avoid excessive forward leaning.\nIncrease resistance gradually instead of pedaling too fast with low resistance.\nStay hydrated and stretch after the session to prevent stiffness.','✅ Available'),(10,'http://localhost:9090/files/1740839917896-Rowing Machine.jpg','Rowing Machine 01','D','D001','Keep your back straight and engage your core while rowing.\nPush with your legs first, then pull with your arms to maintain proper form.\nAvoid excessive hunching or overextending your arms.\nSet the resistance according to your fitness level and increase progressively.\nPerform a proper cooldown to reduce muscle stiffness.','? In Use'),(11,'http://localhost:9090/files/1740840117085-Rowing Machine.jpg','Rowing Machine 02','D','D002','Keep your back straight and engage your core while rowing.\nPush with your legs first, then pull with your arms to maintain proper form.\nAvoid excessive hunching or overextending your arms.\nSet the resistance according to your fitness level and increase progressively.\nPerform a proper cooldown to reduce muscle stiffness.','✅ Available'),(12,'http://localhost:9090/files/1740840211102-Rowing Machine.jpg','Rowing Machine 03','D','D003','Keep your back straight and engage your core while rowing.\nPush with your legs first, then pull with your arms to maintain proper form.\nAvoid excessive hunching or overextending your arms.\nSet the resistance according to your fitness level and increase progressively.\nPerform a proper cooldown to reduce muscle stiffness.','✅ Available'),(13,'http://localhost:9090/files/1740840275580-Stair Climber.jpg','Stair Climber 01','E','E001','Keep a natural posture and avoid leaning too much on the handrails.\nStep with your whole foot instead of using only the toes.\nAdjust the speed to maintain a steady, controlled movement.\nEngage your core and avoid locking your knees when stepping.\nHydrate well and take breaks if you feel excessive fatigue.','? In Use'),(14,'http://localhost:9090/files/1740840334365-Stair Climber.jpg','Stair Climber 02','E','E002','Keep a natural posture and avoid leaning too much on the handrails.\nStep with your whole foot instead of using only the toes.\nAdjust the speed to maintain a steady, controlled movement.\nEngage your core and avoid locking your knees when stepping.\nHydrate well and take breaks if you feel excessive fatigue.','✅ Available'),(15,'http://localhost:9090/files/1740840573051-Smith Machine.jpg','Smith Machine 01','F','F001','Adjust the barbell height before starting to lift.\nEnsure your feet are properly positioned to avoid knee strain.\nEngage your core and avoid excessive leaning forward or backward.\nUse proper weights and lock the bar in place when done.\nHave a spotter if lifting heavy weights to ensure safety.\n','? In Use'),(16,'http://localhost:9090/files/1740840591404-Smith Machine.jpg','Smith Machine 02','F','F002','Adjust the barbell height before starting to lift.\nEnsure your feet are properly positioned to avoid knee strain.\nEngage your core and avoid excessive leaning forward or backward.\nUse proper weights and lock the bar in place when done.\nHave a spotter if lifting heavy weights to ensure safety.\n','✅ Available'),(17,'http://localhost:9090/files/1740840647603-Smith Machine.jpg','Smith Machine 03','F','F003','Adjust the barbell height before starting to lift.\nEnsure your feet are properly positioned to avoid knee strain.\nEngage your core and avoid excessive leaning forward or backward.\nUse proper weights and lock the bar in place when done.\nHave a spotter if lifting heavy weights to ensure safety.\n','✅ Available'),(18,'http://localhost:9090/files/1740840708809-Squat Rack.jpg','Squat Rack 01','G','G001','Set the safety bars at an appropriate height before starting.\nMaintain proper squat form: feet shoulder-width apart, knees tracking over toes.\nEngage your core and keep your back straight during lifts.\nDo not overload the bar beyond your capacity to prevent injuries.\nAlways re-rack the weights after use to keep the area organized.','✅ Available'),(19,'http://localhost:9090/files/1740840738517-Squat Rack.jpg','Squat Rack 02','G','G002','Set the safety bars at an appropriate height before starting.\nMaintain proper squat form: feet shoulder-width apart, knees tracking over toes.\nEngage your core and keep your back straight during lifts.\nDo not overload the bar beyond your capacity to prevent injuries.\nAlways re-rack the weights after use to keep the area organized.','✅ Available'),(20,'http://localhost:9090/files/1740840918644-Squat Rack.jpg','Squat Rack 03','G','G003','Set the safety bars at an appropriate height before starting.\nMaintain proper squat form: feet shoulder-width apart, knees tracking over toes.\nEngage your core and keep your back straight during lifts.\nDo not overload the bar beyond your capacity to prevent injuries.\nAlways re-rack the weights after use to keep the area organized.','✅ Available'),(21,'http://localhost:9090/files/1740841029302-Cable Machine.jpg','Cable Machine 01','H','H001','Adjust the pulley height and weight stack before starting.\nUse controlled movements to prevent momentum-based injuries.\nMaintain a stable stance and engage your core during exercises.\nAvoid excessive jerking or pulling too fast.\nClean the handles and attachments after use for hygiene.\n','? In Use'),(22,'http://localhost:9090/files/1740841105319-Cable Machine.jpg','Cable Machine 02','H','H002','Adjust the pulley height and weight stack before starting.\nUse controlled movements to prevent momentum-based injuries.\nMaintain a stable stance and engage your core during exercises.\nAvoid excessive jerking or pulling too fast.\nClean the handles and attachments after use for hygiene.\n','✅ Available'),(23,'http://localhost:9090/files/1740841124735-Cable Machine.jpg','Cable Machine 03','H','H003','Adjust the pulley height and weight stack before starting.\nUse controlled movements to prevent momentum-based injuries.\nMaintain a stable stance and engage your core during exercises.\nAvoid excessive jerking or pulling too fast.\nClean the handles and attachments after use for hygiene.\n','✅ Available'),(24,'http://localhost:9090/files/1740841700276-Pec Deck Machine.jpg','Pec Deck Machine 01','I','I001','Adjust the thigh pads to keep your body stable.\nGrip the bar slightly wider than shoulder-width.\nPull the bar down to your chest with controlled motion.\nAvoid using momentum or leaning too far back.\nRelease the bar slowly to prevent injury.\n','✅ Available'),(25,'http://localhost:9090/files/1740841714970-Pec Deck Machine.jpg','Pec Deck Machine 02','I','I002',NULL,'✅ Available'),(26,'http://localhost:9090/files/1740842002788-Lat Pulldown Machine.jpg','Lat Pulldown 01','J','J001','Adjust the thigh pads to keep your body stable.\nGrip the bar slightly wider than shoulder-width.\nPull the bar down to your chest with controlled motion.\nAvoid using momentum or leaning too far back.\nRelease the bar slowly to prevent injury.\n','✅ Available'),(27,'http://localhost:9090/files/1740842040631-Lat Pulldown Machine.jpg','Lat Pulldown 02','J','J002','Adjust the thigh pads to keep your body stable.\nGrip the bar slightly wider than shoulder-width.\nPull the bar down to your chest with controlled motion.\nAvoid using momentum or leaning too far back.\nRelease the bar slowly to prevent injury.\n','✅ Available'),(28,'http://localhost:9090/files/1740842068415-Roman Chair.jpg','Roman Chair 01','K','K001','Adjust the pads to fit your height comfortably.\nKeep your back straight and engage your core during movements.\nAvoid excessive hyperextension of the lower back.\nUse slow, controlled movements to prevent strain.\nAdd resistance gradually if needed for progression.','✅ Available'),(29,'http://localhost:9090/files/1740842101182-Roman Chair.jpg','Roman Chair 02','K','K002','Adjust the pads to fit your height comfortably.\nKeep your back straight and engage your core during movements.\nAvoid excessive hyperextension of the lower back.\nUse slow, controlled movements to prevent strain.\nAdd resistance gradually if needed for progression.','✅ Available'),(30,'http://localhost:9090/files/1740842154967-Swimming Pool.jpg','Swimming Pool','L','L001','Always warm up before entering the water.\nFollow the gym’s pool safety guidelines and designated swim lanes.\nAvoid running around the pool to prevent slipping.\nUse proper swim gear, such as goggles and swim caps.\nStay hydrated and take breaks when necessary.','? In Use'),(31,'http://localhost:9090/files/1740842175733-Swimming Pool.jpg','Swimming Pool','L','L002','Always warm up before entering the water.\nFollow the gym’s pool safety guidelines and designated swim lanes.\nAvoid running around the pool to prevent slipping.\nUse proper swim gear, such as goggles and swim caps.\nStay hydrated and take breaks when necessary.','? In Use'),(32,'http://localhost:9090/files/1740842200598-Swimming Pool.jpg','Swimming Pool','L','L003','Always warm up before entering the water.\nFollow the gym’s pool safety guidelines and designated swim lanes.\nAvoid running around the pool to prevent slipping.\nUse proper swim gear, such as goggles and swim caps.\nStay hydrated and take breaks when necessary.','✅ Available'),(33,'http://localhost:9090/files/1740842230963-Swimming Pool.jpg','Swimming Pool','L','L004','Always warm up before entering the water.\nFollow the gym’s pool safety guidelines and designated swim lanes.\nAvoid running around the pool to prevent slipping.\nUse proper swim gear, such as goggles and swim caps.\nStay hydrated and take breaks when necessary.','✅ Available'),(34,'http://localhost:9090/files/1740842240025-Swimming Pool.jpg','Swimming Pool','L','L005','Always warm up before entering the water.\nFollow the gym’s pool safety guidelines and designated swim lanes.\nAvoid running around the pool to prevent slipping.\nUse proper swim gear, such as goggles and swim caps.\nStay hydrated and take breaks when necessary.','✅ Available'),(35,'http://localhost:9090/files/1740842526062-Child Swimming Pool.jpg','Children’s Pool','M','M001','Ensure children are always supervised by an adult or coach.\nUse floatation devices if needed for safety.\nEncourage children to warm up before swimming.\nTeach basic water safety rules, including no running around the pool.\nKeep the pool area clean and dry to prevent accidents.','✅ Available'),(36,'http://localhost:9090/files/1740842552547-Child Swimming Pool.jpg','Children’s Pool','M','M002','Ensure children are always supervised by an adult or coach.\nUse floatation devices if needed for safety.\nEncourage children to warm up before swimming.\nTeach basic water safety rules, including no running around the pool.\nKeep the pool area clean and dry to prevent accidents.','✅ Available'),(37,'http://localhost:9090/files/1740842558104-Child Swimming Pool.jpg','Children’s Pool','M','M003','Ensure children are always supervised by an adult or coach.\nUse floatation devices if needed for safety.\nEncourage children to warm up before swimming.\nTeach basic water safety rules, including no running around the pool.\nKeep the pool area clean and dry to prevent accidents.','✅ Available');
/*!40000 ALTER TABLE `equipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experience`
--

DROP TABLE IF EXISTS `experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `experience` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'post title',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'post content',
  `user_id` int DEFAULT NULL COMMENT 'user ID',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'user role',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'post time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='post info';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experience`
--

LOCK TABLES `experience` WRITE;
/*!40000 ALTER TABLE `experience` DISABLE KEYS */;
INSERT INTO `experience` VALUES (1,'? First Gym Session Done! ??','<p>Hey everyone! Today marks the beginning of my fitness journey, and I just finished my first workout at the gym! ?️‍♂️?</p><p>I was a little nervous at first, but once I got started, I actually felt pretty good. I focused on some basic exercises like squats, push-ups, and light dumbbell workouts. Definitely felt the burn, but I know it’s all part of the process!</p><p>My goals? Get stronger, build endurance, and most importantly, stay consistent. ?</p><p>If anyone has tips for beginners, feel free to drop them in the comments! Let’s get stronger together! ??</p><p><img src=\"http://localhost:9090/files/1741037876923-bg.jpg\" contenteditable=\"false\" width=\"50%\"/></p><p>#FirstWorkout #FitnessJourney #StayMotivated #GymLife</p>',6,'USER','2025-03-03 21:23:12'),(2,'?️‍♂️ Welcome to Your Fitness Journey! ??','<p>Hey everyone! ? I\'m Coach Leon, and I\'m thrilled to be here as your guide on this incredible fitness journey! Whether you\'re a beginner looking to get started or an experienced athlete aiming to push your limits, this is the place to grow, challenge yourself, and achieve your goals.</p><p><img src=\"http://localhost:9090/files/1741037807548-bg-coach.png\" contenteditable=\"false\" width=\"50%\"/></p><p><br/></p><p>? <strong>What you can expect from me:</strong><br/>✅ Training tips to improve strength &amp; endurance<br/>✅ Workout plans for all levels<br/>✅ Nutrition advice to fuel your progress<br/>✅ Motivation to keep you on track</p><p>Remember, fitness is not just about lifting weights—it\'s about building confidence, discipline, and a healthier lifestyle. Let\'s stay consistent, support each other, and make every workout count!</p><p>Drop a comment and introduce yourself! What are your fitness goals? Let’s crush them together! ??</p><p>#Coach #WelcomeToTheGym #FitnessCommunity #StrongerTogether</p>',2,'COACH','2025-03-03 21:36:48');
/*!40000 ALTER TABLE `experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'title',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'content',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'creation time',
  `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'author',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='bulletin board';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (4,'Welcome','Gym opening! Welcome to Gym Panel!','2025-03-03','admin0'),(5,'Closure Notice','The gym will be closed next Wednesday for a day of rest.','2025-03-03','admin0');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `course_id` int DEFAULT NULL COMMENT 'course id',
  `user_id` int DEFAULT NULL COMMENT 'user id',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'order number',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'time',
  `price` double(10,2) DEFAULT NULL COMMENT 'order price',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,21,6,'20250228084353','2025-02-28 08:43:53',10.00),(2,21,6,'20250228085422','2025-02-28 08:54:22',10.00),(3,21,6,'20250228085426','2025-02-28 08:54:26',10.00),(4,21,6,'20250228085426','2025-02-28 08:54:26',10.00),(6,17,5,'20250228105507','2025-02-28 10:55:07',12.00),(7,13,5,'20250228105513','2025-02-28 10:55:13',14.00),(8,11,5,'ORD796891','2025-02-23 00:00:00',15.40),(9,19,5,'ORD405963','2025-02-16 00:00:00',10.12),(10,21,6,'ORD674232','2025-02-17 00:00:00',16.27),(11,17,6,'ORD167003','2024-12-28 00:00:00',15.45),(12,19,5,'ORD185055','2025-02-27 00:00:00',17.31),(13,16,5,'ORD887620','2025-01-01 00:00:00',16.44),(14,17,5,'ORD161130','2025-01-03 00:00:00',19.84),(15,13,6,'ORD215579','2025-02-04 00:00:00',11.22),(16,19,6,'ORD767498','2025-02-05 00:00:00',16.58),(17,22,6,'ORD301454','2025-01-07 00:00:00',18.15),(18,15,5,'ORD684910','2025-01-13 00:00:00',8.93),(19,11,5,'ORD680062','2024-12-01 00:00:00',14.07),(20,17,5,'ORD181168','2025-01-07 00:00:00',10.61),(21,16,6,'ORD700333','2025-02-14 00:00:00',11.98),(22,22,6,'ORD371814','2025-01-27 00:00:00',13.71),(23,19,6,'ORD609511','2025-02-27 00:00:00',10.53),(24,21,5,'ORD639821','2025-01-23 00:00:00',12.58),(25,22,5,'ORD589178','2025-01-23 00:00:00',12.49),(26,18,5,'ORD940282','2025-01-27 00:00:00',10.64),(27,12,6,'ORD780665','2025-01-25 00:00:00',11.89),(28,16,5,'ORD502949','2024-12-09 00:00:00',17.81),(29,16,5,'ORD463196','2025-01-25 00:00:00',19.97),(30,12,6,'ORD722736','2025-01-21 00:00:00',12.14),(31,19,6,'ORD243224','2024-12-23 00:00:00',14.70),(32,19,5,'ORD365742','2024-12-16 00:00:00',15.87),(33,14,6,'ORD885650','2025-02-20 00:00:00',12.32),(34,22,5,'ORD521374','2025-01-18 00:00:00',8.51),(35,16,6,'ORD283990','2024-12-01 00:00:00',8.07),(36,22,6,'ORD793729','2024-12-05 00:00:00',12.36),(37,16,5,'ORD528082','2024-12-21 00:00:00',11.41),(38,11,5,'ORD496203','2024-11-09 00:00:00',17.94),(39,20,5,'ORD679019','2024-10-19 00:00:00',13.70),(40,16,6,'ORD275915','2024-10-09 00:00:00',14.50),(41,11,5,'ORD965528','2024-10-01 00:00:00',19.28),(42,11,5,'ORD181244','2024-11-08 00:00:00',11.66),(43,11,5,'ORD334922','2024-11-19 00:00:00',17.15),(44,16,5,'ORD486788','2024-12-04 00:00:00',11.58),(45,14,6,'ORD856340','2024-10-10 00:00:00',15.61),(46,16,6,'ORD717699','2024-10-10 00:00:00',17.17),(47,21,5,'ORD304032','2024-11-18 00:00:00',17.65),(48,14,6,'ORD508768','2024-10-19 00:00:00',11.50),(49,15,6,'ORD390131','2024-11-24 00:00:00',9.66),(50,11,6,'ORD357728','2024-10-30 00:00:00',19.79),(51,18,5,'ORD509112','2024-11-18 00:00:00',11.52),(52,16,5,'ORD201358','2024-10-13 00:00:00',14.96),(53,18,6,'ORD178743','2024-10-28 00:00:00',19.35),(54,16,5,'ORD698999','2024-10-30 00:00:00',19.85),(55,11,5,'ORD970101','2024-12-08 00:00:00',16.08),(56,16,6,'ORD605189','2024-12-05 00:00:00',10.86),(57,11,6,'ORD524432','2024-12-22 00:00:00',9.74),(58,18,6,'ORD489014','2024-11-17 00:00:00',8.50),(59,13,5,'ORD834040','2024-12-23 00:00:00',14.84),(60,14,5,'ORD972730','2024-11-02 00:00:00',17.45),(61,16,5,'ORD862627','2024-10-05 00:00:00',10.92),(62,16,6,'ORD913381','2024-10-26 00:00:00',13.23),(63,21,6,'ORD234933','2024-10-21 00:00:00',10.67),(64,19,6,'ORD301855','2024-10-21 00:00:00',10.31),(65,17,6,'ORD335238','2024-11-17 00:00:00',17.86),(66,19,6,'ORD770633','2024-11-03 00:00:00',15.14),(67,14,6,'ORD548077','2024-11-21 00:00:00',18.40),(68,15,5,'20250302081426','2025-03-02 08:14:26',0.00),(69,18,5,'20250302081434','2025-03-02 08:14:34',14.00),(70,20,6,'20250304214127','2025-03-04 21:41:27',11.00),(71,11,7,'20250304221548','2025-03-04 22:15:48',15.00),(72,18,7,'20250304221644','2025-03-04 22:16:44',14.00),(73,21,6,'20250305155508','2025-03-05 15:55:08',10.00),(74,21,6,'20250309110601','2025-03-09 11:06:00',10.00),(75,16,5,'20250311033431','2025-03-11 03:34:31',14.00),(76,13,5,'20250311033634','2025-03-11 03:36:34',14.00),(77,13,5,'20250311033729','2025-03-11 03:37:29',14.00),(78,21,5,'20250311034950','2025-03-11 03:49:50',10.00),(79,21,5,'20250311035016','2025-03-11 03:50:16',10.00),(80,21,5,'20250311035748','2025-03-11 03:57:48',10.00),(81,21,5,'20250311040040','2025-03-11 04:00:40',10.00),(82,12,5,'20250311040227','2025-03-11 04:02:27',12.00),(83,22,5,'20250311041449','2025-03-11 04:14:49',9.00),(84,22,5,'20250311041702','2025-03-11 04:17:02',9.00),(85,15,5,'20250311044432','2025-03-11 04:44:32',0.00),(86,12,5,'20250311045351','2025-03-11 04:53:51',12.00),(87,22,5,'20250311050050','2025-03-11 05:00:50',9.00),(88,22,6,'20250311050902','2025-03-11 05:09:02',9.00),(89,22,6,'20250311083432','2025-03-11 08:34:32',9.00);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reserve`
--

DROP TABLE IF EXISTS `reserve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserve` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int DEFAULT NULL COMMENT 'user ID',
  `coach_id` int DEFAULT NULL COMMENT 'coach ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'reservation info',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='coach reservation tablecoach';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserve`
--

LOCK TABLES `reserve` WRITE;
/*!40000 ALTER TABLE `reserve` DISABLE KEYS */;
INSERT INTO `reserve` VALUES (1,6,2,'Hi, Ms Wong','2025-02-27 20:27:28'),(2,5,3,'Hi I want book a training lesson on this Friday! Are you free that day? I look forward to hearing from you.','2025-02-28 10:04:11'),(3,6,6,'Hi Coach, I\'d like to train my core on Sunday night.','2025-03-02 07:19:17'),(4,6,3,'Hello coach! I want to train Pilates tomorrow at 4pm!','2025-03-02 07:19:52'),(5,6,3,'Are you free the day after tomorrow? I\'d like to review my previous lessons.','2025-03-02 07:20:25'),(6,6,5,'Hello! I\'d like to start my recovery from an injury, are you free next Wednesday?','2025-03-02 07:21:06'),(7,6,2,'Hello! I\'d like to come and train next Monday at 7pm, do you have time?','2025-03-02 07:21:43'),(8,6,5,'Hi coach! Tomorrow Evening, are u available?','2025-03-03 20:55:19');
/*!40000 ALTER TABLE `reserve` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'userID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'user username',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'user password',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'user name',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'user avatar',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'role',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'phone',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'email',
  `account` double(10,2) DEFAULT '0.00' COMMENT 'balance',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='user info table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (5,'user0','123','Sam','http://localhost:9090/files/1741042073957-avatar-user..png','USER','0123456789','mizi99207@gmail.com',139.00),(6,'user1','123','Jing','http://localhost:9090/files/1741042088305-avatar-user..png','USER','0123456789','mizi99207@gmail.com',64.00),(7,'user2','123','Geralt','http://localhost:9090/files/1741042255029-avatar-user..png','USER','0123456789','user2@example.com',76.00);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-17 19:37:00
