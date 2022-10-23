-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: my-bbs
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_advertisement`
--

DROP TABLE IF EXISTS `t_advertisement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_advertisement` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(50) NOT NULL,
  `url` varchar(300) DEFAULT NULL COMMENT '跳转目标',
  `image_file_id` bigint unsigned NOT NULL,
  `sequence` int unsigned NOT NULL DEFAULT '0' COMMENT '顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_advertising_image_file_id_idx` (`image_file_id`),
  CONSTRAINT `fk_advertising_image_file_id` FOREIGN KEY (`image_file_id`) REFERENCES `t_file` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页广告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_advertisement`
--

LOCK TABLES `t_advertisement` WRITE;
/*!40000 ALTER TABLE `t_advertisement` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_advertisement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_attachment`
--

DROP TABLE IF EXISTS `t_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_attachment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL COMMENT '指向贴子',
  `comment_id` bigint DEFAULT NULL COMMENT '指向该贴子中的评论(若为null)，则表示指向贴子内容',
  `file_id` bigint unsigned NOT NULL COMMENT '指向属于于该贴子的文件记录',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_used_file_id_idx` (`file_id`),
  CONSTRAINT `fk_used_file_id` FOREIGN KEY (`file_id`) REFERENCES `t_file` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录贴子内容或其评论中的图片等附加文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_attachment`
--

LOCK TABLES `t_attachment` WRITE;
/*!40000 ALTER TABLE `t_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_authority`
--

DROP TABLE IF EXISTS `t_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_authority` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` char(50) DEFAULT NULL COMMENT '接口名称',
  `authority` char(50) NOT NULL COMMENT '权限标识',
  `category` char(20) DEFAULT NULL COMMENT '分类',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `valid` bit(1) NOT NULL DEFAULT b'1' COMMENT '该权限是否有效(在@PreAuthorize中使用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `authority_UNIQUE` (`authority`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_authority`
--

LOCK TABLES `t_authority` WRITE;
/*!40000 ALTER TABLE `t_authority` DISABLE KEYS */;
INSERT INTO `t_authority` VALUES (1,'修改板块','admin:forum:update','板块管理',NULL,_binary '','2022-08-29 16:03:51','2022-09-01 23:29:03'),(2,'添加角色','admin:role:add','角色管理','添加角色',_binary '','2022-08-29 16:03:51','2022-08-30 11:44:59'),(4,'删除评论','admin:comment:remove','评论管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 11:46:48'),(5,'修改板块logo','admin:forum:logo:update','板块管理',NULL,_binary '','2022-08-29 16:03:51','2022-09-01 23:29:09'),(6,'获取统计信息','admin:statistic:get','统计',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 11:47:16'),(7,'更新用户状态','admin:user:status:update','用户管理','更新用户状态(启用/禁用)',_binary '','2022-08-29 16:03:51','2022-08-30 12:25:35'),(9,'评论列表','admin:comment:list','评论管理','列出所有评论',_binary '','2022-08-29 16:03:51','2022-08-30 12:26:01'),(12,'修改角色','admin:role:update','用户管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 12:27:59'),(13,'修改用户','admin:user:update','角色管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 12:28:03'),(14,'角色列表','admin:role:list','角色管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 12:28:31'),(15,'添加板块','admin:forum:add','板块管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 12:28:40'),(16,'修改用户','admin:user::update','用户管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-31 21:57:10'),(17,'删除贴子','admin:post:remove','贴子管理',NULL,_binary '','2022-08-29 16:03:51','2022-08-30 12:29:26'),(21,'获取角色','admin:role:get','角色管理',NULL,_binary '','2022-08-29 21:38:37','2022-08-30 12:29:38'),(22,'权限列表','admin:authority:list','角色管理',NULL,_binary '','2022-08-29 21:38:37','2022-08-30 12:29:52'),(23,'修改权限','admin:authority:update','角色管理',NULL,_binary '','2022-08-30 10:55:04','2022-08-30 12:30:11'),(24,'板块分类列表','admin:forum:category:list','板块管理',NULL,_binary '','2022-08-30 12:35:18','2022-08-30 12:36:04'),(25,'添加广告','admin:sys:ads:add','广告管理','添加首页走马灯广告',_binary '','2022-08-31 14:42:59','2022-08-31 21:57:56'),(26,'修改广告','admin:sys:ads:update','广告管理',NULL,_binary '','2022-08-31 14:42:59','2022-08-31 21:58:15'),(27,'删除广告','admin:sys:ads:remove','广告管理',NULL,_binary '','2022-08-31 14:42:59','2022-08-31 21:58:24'),(28,'删除权限','admin:authority:remove','角色管理','删除失效权限',_binary '','2022-08-31 21:55:02','2022-08-31 21:58:56'),(29,'删除角色','admin:role:remove','角色管理',NULL,_binary '','2022-08-31 22:12:23','2022-08-31 22:12:36'),(30,'设置头像','user:set-avatar','用户',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:27:15'),(31,'贴子点赞','post:like','贴子',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:22:38'),(32,'板块列表','forum:list','板块',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:20:04'),(33,'评论查看','comment:list','评论','查看贴子中的评论',_binary '','2022-09-01 23:13:09','2022-09-01 23:16:53'),(34,'我的评论列表','comment:list:mine','评论','查看该用户所发表评论',_binary '','2022-09-01 23:13:09','2022-09-01 23:17:38'),(35,'删除所发贴子','post:remove:mine','贴子',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:23:00'),(36,'用户注册','user:register','用户','注册用户',_binary '','2022-09-01 23:13:09','2022-09-01 23:26:43'),(37,'删除所发评论','comment:remove:mine','评论','删除该用户所发表的评论',_binary '','2022-09-01 23:13:09','2022-09-01 23:18:16'),(38,'评论点赞','comment:like','评论','',_binary '','2022-09-01 23:13:09','2022-09-01 23:18:31'),(39,'获取板块','forum:get','板块','',_binary '','2022-09-01 23:13:09','2022-09-01 23:20:16'),(40,'贴子列表','post:list','贴子','板块中的贴子列表',_binary '','2022-09-01 23:13:09','2022-09-01 23:23:36'),(41,'用户列表','user:lists','用户','',_binary '','2022-09-01 23:13:09','2022-09-01 23:27:06'),(42,'查看贴子','post:get','贴子','查看贴子内容',_binary '','2022-09-01 23:13:09','2022-09-01 23:24:21'),(43,'贴子搜索','post:search','贴子','',_binary '','2022-09-01 23:13:09','2022-09-01 23:24:55'),(44,'发表评论','comment:add','评论','',_binary '','2022-09-01 23:13:09','2022-09-01 23:18:48'),(45,'发帖','post:add','贴子',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:25:06'),(46,'发送私信','message:send','消息','',_binary '\0','2022-09-01 23:13:09','2022-09-01 23:20:54'),(47,'获取用户','user:get','用户',NULL,_binary '','2022-09-01 23:13:09','2022-09-01 23:27:24'),(48,'上传贴子中插图','post:image:upload','贴子','上传图片以在贴子内容中引用',_binary '','2022-09-01 23:13:09','2022-09-01 23:26:07'),(49,'私信查看','message:private:list','消息','',_binary '\0','2022-09-01 23:13:09','2022-09-01 23:21:34'),(50,'会话列表','message:conversation:list','消息','',_binary '\0','2022-09-01 23:13:09','2022-09-01 23:21:51'),(51,NULL,'chat:private:list',NULL,NULL,_binary '','2022-10-22 19:40:35',NULL),(52,NULL,'chat:conversation:list',NULL,NULL,_binary '','2022-10-22 19:40:36',NULL);
/*!40000 ALTER TABLE `t_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_comment`
--

DROP TABLE IF EXISTS `t_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_comment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) NOT NULL,
  `author_id` bigint unsigned NOT NULL,
  `parent_id` bigint unsigned DEFAULT NULL COMMENT '所属的一级评论',
  `replied_id` bigint unsigned DEFAULT NULL COMMENT '被回复的评论',
  `post_id` bigint unsigned NOT NULL COMMENT '所属贴子id',
  `like_count` int unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_author_id_idx` (`author_id`),
  KEY `fk_post_id_idx` (`post_id`),
  CONSTRAINT `fk_author_id` FOREIGN KEY (`author_id`) REFERENCES `t_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_post_id` FOREIGN KEY (`post_id`) REFERENCES `t_post` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_comment`
--

LOCK TABLES `t_comment` WRITE;
/*!40000 ALTER TABLE `t_comment` DISABLE KEYS */;
INSERT INTO `t_comment` VALUES (78,'测试子评论1-1',31,NULL,NULL,16,0,'2022-10-20 17:12:35',NULL),(79,'测试子评论1-1',31,NULL,NULL,16,0,'2022-10-20 17:13:30',NULL),(80,'测试子评论1-1',41,NULL,NULL,16,0,'2022-10-21 18:58:52',NULL),(81,'测试子评论1-1',41,NULL,NULL,16,0,'2022-10-21 19:01:31',NULL);
/*!40000 ALTER TABLE `t_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_comment_like`
--

DROP TABLE IF EXISTS `t_comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_comment_like` (
  `user_id` bigint unsigned NOT NULL,
  `comment_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`comment_id`),
  KEY `fk_comment_like_user_id_idx` (`user_id`),
  KEY `fk_liked_comment_id_idx` (`comment_id`),
  CONSTRAINT `fk_comment_like_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_liked_comment_id` FOREIGN KEY (`comment_id`) REFERENCES `t_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户对评论的点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_comment_like`
--

LOCK TABLES `t_comment_like` WRITE;
/*!40000 ALTER TABLE `t_comment_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_comment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_conversation`
--

DROP TABLE IF EXISTS `t_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_conversation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user1_id` bigint unsigned NOT NULL COMMENT '规定两个用户ID中较小的放在user1ID中',
  `user2_id` bigint unsigned NOT NULL COMMENT '规定两个用户ID中较大的放在user1ID中',
  `last_message_id` bigint unsigned DEFAULT NULL COMMENT '最新一条消息',
  `last_message_time` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最后一条消息的时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_conversation_users` (`user1_id`,`user2_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户私信会话信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_conversation`
--

LOCK TABLES `t_conversation` WRITE;
/*!40000 ALTER TABLE `t_conversation` DISABLE KEYS */;
INSERT INTO `t_conversation` VALUES (1,31,32,9,'2022-10-23 16:59:22.005000'),(2,31,33,4,'2022-10-23 16:47:22.502000');
/*!40000 ALTER TABLE `t_conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file`
--

DROP TABLE IF EXISTS `t_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `filepath` varchar(100) NOT NULL COMMENT '文件路径(文件的key)',
  `type` char(10) NOT NULL COMMENT '文件类型(后缀)',
  `uploader_id` bigint unsigned DEFAULT NULL COMMENT '上传者id',
  `is_temporary` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否为临时文件',
  `source` char(20) DEFAULT NULL COMMENT '文件来源(分类)(用于标识用途)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_uploader_id_idx` (`uploader_id`),
  KEY `idx_temp_flag` (`is_temporary`) COMMENT '快速查询临时文件',
  KEY `idx_filepath` (`filepath`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file`
--

LOCK TABLES `t_file` WRITE;
/*!40000 ALTER TABLE `t_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_forum`
--

DROP TABLE IF EXISTS `t_forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_forum` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` char(10) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `category` char(10) NOT NULL COMMENT '板块分类',
  `logo_file_id` bigint unsigned DEFAULT NULL COMMENT 'logo图片文件id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `idx_forum_category` (`category`),
  KEY `fk_logo_file_id_idx` (`logo_file_id`),
  CONSTRAINT `fk_logo_file_id` FOREIGN KEY (`logo_file_id`) REFERENCES `t_file` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='板块';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_forum`
--

LOCK TABLES `t_forum` WRITE;
/*!40000 ALTER TABLE `t_forum` DISABLE KEYS */;
INSERT INTO `t_forum` VALUES (20,'测试','测试','测试',NULL,'2022-10-15 21:20:31',NULL);
/*!40000 ALTER TABLE `t_forum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_message`
--

DROP TABLE IF EXISTS `t_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `sender_id` bigint unsigned DEFAULT NULL COMMENT '发送者id(为空表示系统消息)',
  `receiver_id` bigint unsigned NOT NULL,
  `conversation_id` binary(16) DEFAULT NULL COMMENT '用于表示两个用户间的会话，用于获取会话列表时groupBy.sender_id和receiver_id中，较大的放在高8字节，较小的放在低8字节。',
  `type` char(10) NOT NULL COMMENT '消息类型',
  `is_acknowledged` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已读',
  `content` varchar(500) NOT NULL COMMENT '消息内容',
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `fk_message_receiver_id` (`receiver_id`),
  KEY `fk_message_sender_id` (`sender_id`),
  KEY `idx_message_send_time` (`create_time` DESC),
  KEY `idx_acknowledge_flag` (`is_acknowledged`),
  CONSTRAINT `fk_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_message`
--

LOCK TABLES `t_message` WRITE;
/*!40000 ALTER TABLE `t_message` DISABLE KEYS */;
INSERT INTO `t_message` VALUES (65,41,31,NULL,'Reply',_binary '','{\"authorId\":41,\"authorUsername\":\"user10\",\"comment\":\"测试子评论1-1\",\"commentId\":81,\"postId\":16,\"postTitle\":\"测试\"}','2022-10-21 19:01:31.868000',NULL);
/*!40000 ALTER TABLE `t_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_post`
--

DROP TABLE IF EXISTS `t_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_post` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `author_id` bigint unsigned NOT NULL COMMENT '发布者',
  `forum_id` bigint unsigned NOT NULL COMMENT '所属板块',
  `like_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `view_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `comment_count` bigint unsigned NOT NULL DEFAULT '0' COMMENT '评论数量',
  `score` bigint unsigned NOT NULL DEFAULT '0' COMMENT '热度分值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_forum_id_idx` (`forum_id`),
  KEY `idx_score` (`score` DESC),
  KEY `idx_post_create_time` (`create_time` DESC),
  CONSTRAINT `fk_forum_id` FOREIGN KEY (`forum_id`) REFERENCES `t_forum` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='贴子';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_post`
--

LOCK TABLES `t_post` WRITE;
/*!40000 ALTER TABLE `t_post` DISABLE KEYS */;
INSERT INTO `t_post` VALUES (16,'测试','测试',31,20,19,11,4,138092,'2022-10-15 21:29:45',NULL),(17,'测试2','测试',31,20,0,0,0,138096,'2022-10-15 21:29:55',NULL),(18,'测试3','测试',31,20,0,0,0,138093,'2022-10-15 21:29:59',NULL),(19,'测试4','测试',31,20,0,0,0,138097,'2022-10-15 21:30:01',NULL),(20,'测试4','测试',31,20,0,0,0,138094,'2022-10-15 21:30:02',NULL),(21,'测试4','测试',31,20,0,0,0,138095,'2022-10-15 21:30:03',NULL),(22,'测试4','测试',31,20,0,0,0,138092,'2022-10-15 21:30:03',NULL),(23,'测试4','测试',31,20,0,0,0,138101,'2022-10-15 21:30:04',NULL),(24,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:30:04',NULL),(25,'测试4','测试',31,20,0,0,0,138096,'2022-10-15 21:30:05',NULL),(26,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:05',NULL),(27,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:06',NULL),(28,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:07',NULL),(29,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:07',NULL),(30,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:08',NULL),(31,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:08',NULL),(32,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:09',NULL),(33,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:09',NULL),(34,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:10',NULL),(35,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:11',NULL),(36,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:11',NULL),(37,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:12',NULL),(38,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:12',NULL),(39,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:13',NULL),(40,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:13',NULL),(41,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:14',NULL),(42,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:14',NULL),(43,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:15',NULL),(44,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:15',NULL),(45,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:16',NULL),(46,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:16',NULL),(47,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:17',NULL),(48,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:17',NULL),(49,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:33',NULL),(50,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:41',NULL),(51,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:41',NULL),(52,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:42',NULL),(53,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:43',NULL),(54,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:44',NULL),(55,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:45',NULL),(56,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:46',NULL),(57,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:46',NULL),(58,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:47',NULL),(59,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:48',NULL),(60,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:49',NULL),(61,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:49',NULL),(62,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:50',NULL),(63,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:51',NULL),(64,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:52',NULL),(65,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:53',NULL),(66,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:53',NULL),(67,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:54',NULL),(68,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:55',NULL),(69,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:56',NULL),(70,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:56',NULL),(71,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:57',NULL),(72,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:58',NULL),(73,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:30:59',NULL),(74,'测试4','测试',31,20,0,0,0,138090,'2022-10-15 21:31:00',NULL),(75,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:00',NULL),(76,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:23',NULL),(77,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:24',NULL),(78,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:25',NULL),(79,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:26',NULL),(80,'测试4','测试',31,20,0,0,0,138091,'2022-10-15 21:31:27',NULL),(81,'测试','测试',31,20,0,0,0,145015,'2022-10-20 16:55:08',NULL),(82,'测试','测试',31,20,0,0,0,145018,'2022-10-20 16:58:42',NULL),(83,'测试','测试',31,20,0,0,0,145019,'2022-10-20 16:59:20',NULL),(84,'测试','测试',31,20,0,0,0,145025,'2022-10-20 17:05:36',NULL),(85,'测试','测试',31,20,0,0,0,146487,'2022-10-21 17:27:35',NULL);
/*!40000 ALTER TABLE `t_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_post_like`
--

DROP TABLE IF EXISTS `t_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_post_like` (
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`post_id`),
  KEY `fk_post_like_user_id_idx` (`user_id`),
  KEY `fk_liked_post_id_idx` (`post_id`) /*!80000 INVISIBLE */,
  CONSTRAINT `fk_liked_post_id` FOREIGN KEY (`post_id`) REFERENCES `t_post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_like_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户对贴子的点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_post_like`
--

LOCK TABLES `t_post_like` WRITE;
/*!40000 ALTER TABLE `t_post_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_private_message`
--

DROP TABLE IF EXISTS `t_private_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_private_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `sender_id` bigint unsigned NOT NULL,
  `receiver_id` bigint unsigned NOT NULL,
  `conversation_id` bigint unsigned NOT NULL,
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '消息的类型(0:文本消息)',
  `is_acknowledged` bit(1) NOT NULL DEFAULT b'0',
  `content` varchar(500) NOT NULL,
  `create_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '即发送时间',
  PRIMARY KEY (`id`),
  KEY `fk_private_message_sender_id_idx` (`sender_id`),
  KEY `fk_private_message_receiver_id_idx` (`receiver_id`),
  KEY `idx_private_message_send_time` (`create_time` DESC),
  KEY `fk_conversation_id_idx` (`conversation_id`),
  CONSTRAINT `fk_private_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `fk_private_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户私信';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_private_message`
--

LOCK TABLES `t_private_message` WRITE;
/*!40000 ALTER TABLE `t_private_message` DISABLE KEYS */;
INSERT INTO `t_private_message` VALUES (1,31,32,1,0,_binary '\0','测试','2022-10-23 16:42:40.314000'),(2,31,32,1,0,_binary '\0','测试','2022-10-23 16:46:42.426000'),(3,31,32,1,0,_binary '\0','测试2','2022-10-23 16:46:56.012000'),(4,31,33,2,0,_binary '\0','测试3','2022-10-23 16:47:22.502000'),(5,32,31,1,0,_binary '\0','测试','2022-10-23 16:58:54.542000'),(6,31,32,1,0,_binary '\0','测试','2022-10-23 16:59:16.897000'),(7,31,32,1,0,_binary '\0','测试测试','2022-10-23 16:59:18.428000'),(8,31,32,1,0,_binary '\0','测试测试','2022-10-23 16:59:19.421000'),(9,31,32,1,0,_binary '\0','测试测试','2022-10-23 16:59:22.005000');
/*!40000 ALTER TABLE `t_private_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role`
--

DROP TABLE IF EXISTS `t_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` char(32) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role`
--

LOCK TABLES `t_role` WRITE;
/*!40000 ALTER TABLE `t_role` DISABLE KEYS */;
INSERT INTO `t_role` VALUES (0,'ROLE_ANONYMOUS','2022-10-15 21:00:05','2022-10-15 21:00:05'),(1,'ROLE_NORMAL','2022-10-15 21:00:05','2022-10-15 21:00:05'),(2,'ROLE_ADMIN','2022-10-15 21:00:05','2022-10-15 21:00:05');
/*!40000 ALTER TABLE `t_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role_authority`
--

DROP TABLE IF EXISTS `t_role_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role_authority` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `role_id` bigint unsigned NOT NULL,
  `authority_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_role_id_idx` (`role_id`),
  KEY `fk_authority_id_idx` (`authority_id`),
  CONSTRAINT `fk_authority_id` FOREIGN KEY (`authority_id`) REFERENCES `t_authority` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_authority_role_id` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与权限的关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role_authority`
--

LOCK TABLES `t_role_authority` WRITE;
/*!40000 ALTER TABLE `t_role_authority` DISABLE KEYS */;
INSERT INTO `t_role_authority` VALUES (22,2,9,'2022-08-29 21:52:48'),(24,2,12,'2022-08-29 21:52:48'),(25,2,21,'2022-08-29 21:52:48'),(26,2,13,'2022-08-29 21:52:48'),(27,2,14,'2022-08-29 21:52:48'),(28,2,15,'2022-08-29 21:52:48'),(29,2,16,'2022-08-29 21:52:48'),(30,2,17,'2022-08-29 21:52:48'),(45,2,7,'2022-08-29 23:01:46'),(51,2,1,'2022-08-29 23:17:03'),(52,2,2,'2022-08-29 23:17:03'),(53,2,4,'2022-08-29 23:17:03'),(54,2,5,'2022-08-29 23:17:03'),(62,2,23,'2022-08-30 11:42:05'),(63,2,22,'2022-08-30 12:34:28'),(64,2,24,'2022-08-30 12:36:11'),(69,2,25,'2022-08-31 14:45:24'),(70,2,26,'2022-08-31 14:45:24'),(71,2,27,'2022-08-31 14:45:24'),(72,2,28,'2022-08-31 21:56:25'),(73,2,29,'2022-08-31 22:16:00'),(77,2,6,'2022-09-01 22:46:01'),(78,2,30,'2022-09-01 23:29:50'),(79,2,31,'2022-09-01 23:29:50'),(80,2,32,'2022-09-01 23:29:50'),(81,2,33,'2022-09-01 23:29:50'),(82,2,35,'2022-09-01 23:29:50'),(83,2,34,'2022-09-01 23:29:50'),(84,2,36,'2022-09-01 23:29:50'),(85,2,37,'2022-09-01 23:29:50'),(86,2,39,'2022-09-01 23:29:50'),(87,2,38,'2022-09-01 23:29:50'),(88,2,40,'2022-09-01 23:29:50'),(89,2,41,'2022-09-01 23:29:50'),(90,2,42,'2022-09-01 23:29:50'),(91,2,43,'2022-09-01 23:29:50'),(92,2,44,'2022-09-01 23:29:50'),(93,2,45,'2022-09-01 23:29:50'),(94,2,46,'2022-09-01 23:29:50'),(95,2,47,'2022-09-01 23:29:50'),(96,2,48,'2022-09-01 23:29:50'),(97,2,49,'2022-09-01 23:29:50'),(98,2,50,'2022-09-01 23:29:50'),(99,0,43,'2022-09-02 09:58:56'),(101,0,32,'2022-09-02 09:58:56'),(102,0,47,'2022-09-02 09:58:56'),(103,0,33,'2022-09-02 09:58:56'),(104,0,36,'2022-09-02 09:58:56'),(105,0,40,'2022-09-02 09:58:56'),(106,0,39,'2022-09-02 09:58:56'),(107,0,41,'2022-09-02 09:58:56'),(108,0,42,'2022-09-02 09:58:56'),(109,1,30,'2022-09-02 10:02:13'),(110,1,31,'2022-09-02 10:02:13'),(111,1,32,'2022-09-02 10:02:13'),(112,1,33,'2022-09-02 10:02:13'),(113,1,35,'2022-09-02 10:02:13'),(114,1,34,'2022-09-02 10:02:13'),(115,1,36,'2022-09-02 10:02:13'),(116,1,37,'2022-09-02 10:02:13'),(117,1,40,'2022-09-02 10:02:13'),(118,1,39,'2022-09-02 10:02:13'),(119,1,38,'2022-09-02 10:02:13'),(120,1,41,'2022-09-02 10:02:13'),(121,1,42,'2022-09-02 10:02:13'),(122,1,43,'2022-09-02 10:02:13'),(123,1,44,'2022-09-02 10:02:13'),(124,1,45,'2022-09-02 10:02:13'),(125,1,46,'2022-09-02 10:02:13'),(126,1,47,'2022-09-02 10:02:13'),(127,1,48,'2022-09-02 10:02:13'),(128,1,49,'2022-09-02 10:02:13'),(129,1,50,'2022-09-02 10:02:13'),(130,2,51,'2022-10-22 19:42:54'),(131,1,51,'2022-10-22 19:42:54'),(132,2,52,'2022-10-22 19:42:55'),(133,1,51,'2022-10-22 19:42:55');
/*!40000 ALTER TABLE `t_role_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` char(16) NOT NULL,
  `role_id` bigint unsigned NOT NULL,
  `password` varchar(128) DEFAULT NULL,
  `enable` tinyint NOT NULL DEFAULT '1' COMMENT '是否可用',
  `email` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `gender` bit(1) DEFAULT NULL COMMENT '性别(女:0, 男:1)',
  `avatar_file_id` bigint unsigned DEFAULT NULL COMMENT '头像图片文件id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `idx_role_id_idx` (`role_id`),
  KEY `fk_avatar_file_id_idx` (`avatar_file_id`),
  CONSTRAINT `fk_avatar_file_id` FOREIGN KEY (`avatar_file_id`) REFERENCES `t_file` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `fk_role_id` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=1031 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (31,'user0',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:01:13','2022-10-15 21:01:14'),(32,'user1',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:04:18','2022-10-15 21:04:18'),(33,'user2',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:42'),(34,'user3',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:42'),(35,'user4',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:42'),(36,'user5',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:42'),(37,'user6',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(38,'user7',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(39,'user8',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(40,'user9',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(41,'user10',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(42,'user11',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:43'),(43,'user12',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(44,'user13',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(45,'user14',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(46,'user15',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(47,'user16',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(48,'user17',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(49,'user18',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:44'),(50,'user19',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(51,'user20',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(52,'user21',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(53,'user22',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(54,'user23',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(55,'user24',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:45'),(56,'user25',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(57,'user26',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(58,'user27',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(59,'user28',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(60,'user29',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(61,'user30',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(62,'user31',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:46'),(63,'user32',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(64,'user33',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(65,'user34',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(66,'user35',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(67,'user36',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(68,'user37',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:47'),(69,'user38',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(70,'user39',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(71,'user40',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(72,'user41',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(73,'user42',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(74,'user43',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:48'),(75,'user44',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:49'),(76,'user45',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:49'),(77,'user46',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:49'),(78,'user47',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:49'),(79,'user48',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:49'),(80,'user49',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:50'),(81,'user50',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:50'),(82,'user51',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:50'),(83,'user52',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:51'),(84,'user53',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:51'),(85,'user54',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:52'),(86,'user55',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:52'),(87,'user56',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:53'),(88,'user57',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:53'),(89,'user58',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:53'),(90,'user59',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(91,'user60',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(92,'user61',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(93,'user62',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(94,'user63',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(95,'user64',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:54'),(96,'user65',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:55'),(97,'user66',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:55'),(98,'user67',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:55'),(99,'user68',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:55'),(100,'user69',2,'$2a$10$SSGxukmO8UT0L4iHLI1GLucDZSekNpOPLeyA..C.Ldvnrpl0BwaGK',1,NULL,_binary '',NULL,'2022-10-15 21:05:42','2022-10-15 21:05:55');
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-10-23 17:18:22
