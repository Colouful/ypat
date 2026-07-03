CREATE TABLE `t_article` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credate` datetime DEFAULT NULL,
  `describ` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `flag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `imgpath` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plat` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `readtimes` int DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `imgpath` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `err_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `err_code_des` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `openid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `out_trade_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_msg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_fee` int DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credate` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `upddate` datetime DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_invite_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `invite_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `invitee_userid` bigint NOT NULL,
  `inviter_userid` bigint NOT NULL,
  `reward_ppd` int DEFAULT NULL,
  `source` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_userid` (`invitee_userid`),
  KEY `idx_inviter_userid` (`inviter_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_member_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `duration_days` int NOT NULL,
  `out_trade_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `paidAt` datetime DEFAULT NULL,
  `plan_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `plan_id` bigint NOT NULL,
  `price_fen` int NOT NULL,
  `status` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `wx_transaction_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_user_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_member_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `benefits` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credate` datetime DEFAULT NULL,
  `duration_days` int NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `origin_price_fen` int DEFAULT NULL,
  `price_fen` int NOT NULL,
  `sort_no` int DEFAULT NULL,
  `status` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_mess_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credate` datetime DEFAULT NULL,
  `linkwayflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `messviewflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recperid` bigint DEFAULT NULL,
  `sendperid` bigint DEFAULT NULL,
  `ypatid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrcab8u6s39cf992mwyejy2bfr` (`recperid`),
  KEY `FK3ea5aw81u755e539uyyjw4gwa` (`sendperid`),
  KEY `FKhdhe9kbr1pjkfckifdi728wib` (`ypatid`),
  CONSTRAINT `FK3ea5aw81u755e539uyyjw4gwa` FOREIGN KEY (`sendperid`) REFERENCES `t_user` (`id`),
  CONSTRAINT `FKhdhe9kbr1pjkfckifdi728wib` FOREIGN KEY (`ypatid`) REFERENCES `t_ypat_info` (`id`),
  CONSTRAINT `FKrcab8u6s39cf992mwyejy2bfr` FOREIGN KEY (`recperid`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `err_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `err_code_des` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `out_trade_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prepay_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `productid` bigint DEFAULT NULL,
  `result_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_msg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_fee` int DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `currval` int DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oldval` int DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_pub_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createTime` bigint DEFAULT NULL,
  `dateStr` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `event` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `eventKey` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fromUserName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `msgTimes` int DEFAULT NULL,
  `msgType` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ticket` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `toUserName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `date_str` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `event_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `from_user_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `msg_times` int DEFAULT NULL,
  `msg_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_user_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `ppd` int DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatarurl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `birthday` datetime DEFAULT NULL,
  `certcode` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coltimes` int DEFAULT NULL,
  `creditflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nickname` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `openid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ppd` int DEFAULT NULL,
  `profess` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `province` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pubtimes` int DEFAULT NULL,
  `qq` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `realnameflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recmobile` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rectimes` int DEFAULT NULL,
  `regisdate` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wb` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wx` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_user_img` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `imgpath` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeypi5962sq46n0cofrpflatgc` (`userid`),
  CONSTRAINT `FKeypi5962sq46n0cofrpflatgc` FOREIGN KEY (`userid`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_user_member` (
  `user_id` bigint NOT NULL,
  `expire_at` datetime NOT NULL,
  `level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_user_orig` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credate` datetime DEFAULT NULL,
  `openid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_user_ypat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userid` bigint DEFAULT NULL,
  `ypatid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt90oib2j19acbadhd9hef6y1a` (`userid`),
  KEY `FK4nh679ouf6p5fq6lst9d4rgbu` (`ypatid`),
  CONSTRAINT `FK4nh679ouf6p5fq6lst9d4rgbu` FOREIGN KEY (`ypatid`) REFERENCES `t_ypat_info` (`id`),
  CONSTRAINT `FKt90oib2j19acbadhd9hef6y1a` FOREIGN KEY (`userid`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `deleted_flag` int DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `favorite_count` int DEFAULT NULL,
  `is_nationwide` int DEFAULT NULL,
  `like_count` int DEFAULT NULL,
  `media_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  `read_count` int DEFAULT NULL,
  `return_photo_flag` int DEFAULT NULL,
  `shoot_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqq4gl8ej7cbk3hons0jdc06qv` (`userid`),
  CONSTRAINT `FKqq4gl8ej7cbk3hons0jdc06qv` FOREIGN KEY (`userid`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_complain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `height` int DEFAULT NULL,
  `mime` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_no` int DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `upload_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `width` int DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_no` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_work_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `tag_id` bigint DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_ypat_img` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `imgpath` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ypatid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKruybvtxndnypqqpomn5rnrwn` (`ypatid`),
  CONSTRAINT `FKruybvtxndnypqqpomn5rnrwn` FOREIGN KEY (`ypatid`) REFERENCES `t_ypat_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `t_ypat_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `chargeamt` decimal(19,2) DEFAULT NULL,
  `chargeway` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coltimes` int DEFAULT NULL,
  `creditflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `describ` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(19,2) DEFAULT NULL,
  `longitude` decimal(19,2) DEFAULT NULL,
  `patarea` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `patdate` datetime DEFAULT NULL,
  `patslice` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `patstyle` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pattimes` int DEFAULT NULL,
  `province` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pubdate` datetime DEFAULT NULL,
  `readtimes` int DEFAULT NULL,
  `realnameflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recomflag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  `is_nationwide` int DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  `isNationwide` int DEFAULT NULL,
  `workId` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKihn0q6v5g5ogph7hfkd5bpv4r` (`userid`),
  CONSTRAINT `FKihn0q6v5g5ogph7hfkd5bpv4r` FOREIGN KEY (`userid`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
