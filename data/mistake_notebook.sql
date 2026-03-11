/*
 Navicat Premium Data Transfer

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : mistake_notebook

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 19/02/2026 22:04:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for analysis_report
-- ----------------------------
DROP TABLE IF EXISTS `analysis_report`;
CREATE TABLE `analysis_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '分析报告内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_subject`(`user_id` ASC, `subject` ASC) USING BTREE,
  CONSTRAINT `analysis_report_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of analysis_report
-- ----------------------------
INSERT INTO `analysis_report` VALUES (2, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:31:27');
INSERT INTO `analysis_report` VALUES (3, 5, '数据结构', '【学情分析报告】\n本次分析范围：数据结构。\n错题总量：1，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：看错了。\n目前薄弱知识点主要集中在：栈。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:34:48');
INSERT INTO `analysis_report` VALUES (4, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:34:52');
INSERT INTO `analysis_report` VALUES (5, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:34:55');
INSERT INTO `analysis_report` VALUES (6, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:37:23');
INSERT INTO `analysis_report` VALUES (7, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:07');
INSERT INTO `analysis_report` VALUES (8, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:11');
INSERT INTO `analysis_report` VALUES (9, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:12');
INSERT INTO `analysis_report` VALUES (10, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:13');
INSERT INTO `analysis_report` VALUES (11, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:15');
INSERT INTO `analysis_report` VALUES (12, 5, '数学', '【学情分析报告】\n本次分析范围：数学。\n错题总量：1，掌握率：100%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 15:38:17');
INSERT INTO `analysis_report` VALUES (13, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:00:57');
INSERT INTO `analysis_report` VALUES (14, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:01:33');
INSERT INTO `analysis_report` VALUES (15, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:18:53');
INSERT INTO `analysis_report` VALUES (16, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:18:56');
INSERT INTO `analysis_report` VALUES (17, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:19:19');
INSERT INTO `analysis_report` VALUES (18, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 16:20:08');
INSERT INTO `analysis_report` VALUES (19, 4, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：2，掌握率：0%。\n错题最多科目：数学。\n高频错误原因：不会背诵。\n目前薄弱知识点主要集中在：计算题、李白诗句。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:36:34');
INSERT INTO `analysis_report` VALUES (20, 6, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：0，掌握率：0%。\n错题最多科目：暂无。\n高频错误原因：暂无。\n暂未识别到明显薄弱知识点。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:37:18');
INSERT INTO `analysis_report` VALUES (21, 6, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：0，掌握率：0%。\n错题最多科目：暂无。\n高频错误原因：暂无。\n暂未识别到明显薄弱知识点。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:37:44');
INSERT INTO `analysis_report` VALUES (22, 6, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：0，掌握率：0%。\n错题最多科目：暂无。\n高频错误原因：暂无。\n暂未识别到明显薄弱知识点。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:37:58');
INSERT INTO `analysis_report` VALUES (23, 6, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：0，掌握率：0%。\n错题最多科目：暂无。\n高频错误原因：暂无。\n暂未识别到明显薄弱知识点。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:38:03');
INSERT INTO `analysis_report` VALUES (24, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:47:49');
INSERT INTO `analysis_report` VALUES (25, 5, '数学', '【学情分析报告】\n本次分析范围：数学。\n错题总量：1，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:47:57');
INSERT INTO `analysis_report` VALUES (26, 5, '数据结构', '【学情分析报告】\n本次分析范围：数据结构。\n错题总量：1，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：看错了。\n目前薄弱知识点主要集中在：栈。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:48:09');
INSERT INTO `analysis_report` VALUES (27, 5, '数据结构2', '【学情分析报告】\n本次分析范围：数据结构2。\n错题总量：1，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：看错了4214。\n目前薄弱知识点主要集中在：栈。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:48:13');
INSERT INTO `analysis_report` VALUES (28, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:48:33');
INSERT INTO `analysis_report` VALUES (29, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：0%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 20:49:03');
INSERT INTO `analysis_report` VALUES (30, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 22:00:29');
INSERT INTO `analysis_report` VALUES (31, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 22:00:34');
INSERT INTO `analysis_report` VALUES (32, 5, '数学', '【学情分析报告】\n本次分析范围：数学。\n错题总量：1，掌握率：100%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 22:00:39');
INSERT INTO `analysis_report` VALUES (33, 5, NULL, '【学情分析报告】\n本次分析范围：全部科目。\n错题总量：3，掌握率：33%。\n错题最多科目：数据结构2。\n高频错误原因：不会。\n目前薄弱知识点主要集中在：栈、算术。\n建议：优先对薄弱知识点进行专项复习，并结合错因分布优化审题与答题习惯。', '2026-02-19 22:00:40');

-- ----------------------------
-- Table structure for question_content
-- ----------------------------
DROP TABLE IF EXISTS `question_content`;
CREATE TABLE `question_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'manual / ocr',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题目文本',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '正确答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解析',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `question_content_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question_content
-- ----------------------------
INSERT INTO `question_content` VALUES (2, 2, 'manual', '忘记了', '', '');
INSERT INTO `question_content` VALUES (3, 3, 'manual', '忘记了4214', '3213', '532531');
INSERT INTO `question_content` VALUES (4, 4, 'ocr', 'Mock OCR text: test.png', NULL, NULL);
INSERT INTO `question_content` VALUES (5, 5, 'manual', '李白所作诗词中，“君不见黄河之水天上来”的下一句是？(不用加任何标点符号)', '奔流到海不复回', '这题太easy了，还解析啥~');
INSERT INTO `question_content` VALUES (7, 7, 'ocr', '【本地Tesseract识别结果（可能存在误差）】\n1+1=? |', NULL, NULL);

-- ----------------------------
-- Table structure for question_image
-- ----------------------------
DROP TABLE IF EXISTS `question_image`;
CREATE TABLE `question_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ocr_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'pending / success / failed',
  `ocr_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'OCR识别文本',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `question_image_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question_image
-- ----------------------------
INSERT INTO `question_image` VALUES (1, 4, '/uploads/5_1771485163973_21621fb5f07a43f284374e4a55a124d6.png', 'success', 'Mock OCR text: test.png', '2026-02-19 15:12:43');
INSERT INTO `question_image` VALUES (3, 7, '/uploads/4_1771504556723_ec939d4eef9845a1bba1fd68c6d94fac.png', 'success', '【本地Tesseract识别结果（可能存在误差）】\n1+1=? |', '2026-02-19 20:35:57');

-- ----------------------------
-- Table structure for question_tag
-- ----------------------------
DROP TABLE IF EXISTS `question_tag`;
CREATE TABLE `question_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question_tag`(`question_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `tag_id`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `question_tag_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `question_tag_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question_tag
-- ----------------------------
INSERT INTO `question_tag` VALUES (8, 4, 5);
INSERT INTO `question_tag` VALUES (5, 5, 6);
INSERT INTO `question_tag` VALUES (7, 7, 7);

-- ----------------------------
-- Table structure for review_record
-- ----------------------------
DROP TABLE IF EXISTS `review_record`;
CREATE TABLE `review_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `review_count` int NULL DEFAULT 0 COMMENT '已复习次数',
  `last_review_time` datetime NULL DEFAULT NULL COMMENT '上次复习时间',
  `next_review_time` datetime NULL DEFAULT NULL COMMENT '下次复习时间',
  `mastery_level` int NULL DEFAULT 0 COMMENT '掌握程度 0~5',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `review_record_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_record
-- ----------------------------
INSERT INTO `review_record` VALUES (3, 2, 1, '2026-02-19 15:10:39', '2026-02-20 15:10:39', 1);
INSERT INTO `review_record` VALUES (4, 3, 0, '2026-02-19 15:10:46', '2026-02-20 03:10:46', 0);
INSERT INTO `review_record` VALUES (5, 4, 0, '2026-02-19 20:46:59', '2026-02-20 08:46:59', 0);
INSERT INTO `review_record` VALUES (6, 5, 0, NULL, '2026-02-19 16:05:15', 0);
INSERT INTO `review_record` VALUES (8, 7, 0, NULL, '2026-02-19 20:35:57', 0);

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_user`(`name` ASC, `user_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `tag_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (5, '算术', 5);
INSERT INTO `tag` VALUES (4, '计算', 5);
INSERT INTO `tag` VALUES (7, '计算题', 4);
INSERT INTO `tag` VALUES (6, '诗句', 4);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'student / admin',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (3, 'admin', '$2a$10$3FsyzDCD6.YBenQaFh3Tt.KuhEBu9AJWhKj5vtPU22dE6Qd//LNrG', 'admin', NULL, '2026-02-18 17:08:01');
INSERT INTO `user` VALUES (4, 'test', '$2a$10$qad2uYZmoFVNOsY.6OMR2e7mpRSi4aYz6RPx.OA6Q8bBlP6ElqMg.', 'student', NULL, '2026-02-19 08:06:01');
INSERT INTO `user` VALUES (5, 'bob', '$2a$10$3suiuz0rxTVAX49e.VzXwuvH9hBdi2GZ6.3Uv.mw2oyQHOZ4PNMC.', 'student', NULL, '2026-02-19 10:23:23');
INSERT INTO `user` VALUES (6, '张三', '$2a$10$0yJiWM/OGAHrSTwT0Xcr5.uaYlVh.3IVyhjzOM9IwYFQQpdkUOyxC', 'student', NULL, '2026-02-19 20:36:52');

-- ----------------------------
-- Table structure for wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '科目',
  `knowledge_point` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识点',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'manual / image',
  `error_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `difficulty` int NULL DEFAULT NULL COMMENT '1~5',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '未掌握',
  `is_favorite` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_subject`(`subject` ASC) USING BTREE,
  CONSTRAINT `wrong_question_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wrong_question
-- ----------------------------
INSERT INTO `wrong_question` VALUES (2, 5, '数据结构', '栈', 'manual', '看错了', 3, '未掌握', 0, '2026-02-19 14:20:47', '2026-02-19 14:20:47');
INSERT INTO `wrong_question` VALUES (3, 5, '数据结构2', '栈', 'manual', '看错了4214', 4, '未掌握', 1, '2026-02-19 14:21:27', '2026-02-19 14:21:27');
INSERT INTO `wrong_question` VALUES (4, 5, '数学', '算术', 'image', '不会', 5, '已掌握', 1, '2026-02-19 15:12:43', '2026-02-19 21:53:15');
INSERT INTO `wrong_question` VALUES (5, 4, '语文', '李白诗句', 'manual', '不会背诵', 5, '未掌握', 1, '2026-02-19 16:05:15', '2026-02-19 16:05:15');
INSERT INTO `wrong_question` VALUES (7, 4, '数学', '计算题', 'image', '我不太聪明', 5, '未掌握', 1, '2026-02-19 20:35:56', '2026-02-19 20:35:56');

SET FOREIGN_KEY_CHECKS = 1;
