CREATE TABLE IF NOT EXISTS `t_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userid` bigint(20) NOT NULL,
  `content` varchar(500) NOT NULL,
  `contact` varchar(100) DEFAULT NULL,
  `status` varchar(2) NOT NULL DEFAULT '0',
  `credate` datetime NOT NULL,
  `upddate` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feedback_userid` (`userid`),
  KEY `idx_feedback_status_credate` (`status`, `credate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

