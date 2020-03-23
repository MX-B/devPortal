CREATE TABLE `execution_status` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_name___execution_status` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `execution` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `uuid` varchar(42) NOT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NULL,
    `removed_at` datetime NULL,

    `started_at` DATETIME NOT NULL,
    `finished_at` DATETIME,

    `reference_month` int(8) NOT NULL,
    `status_id` int(3) NOT NULL,
    `started_by` VARCHAR(64) NOT NULL,
    `start_trigger` VARCHAR(64) NOT NULL,
    `description` VARCHAR(256) NOT NULL,
    `error_reason` VARCHAR(1024),
    `parameters` VARCHAR(1024) NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_uuid___execution` (`uuid`),
    CONSTRAINT `FK_execution___status` FOREIGN KEY (`status_id`) REFERENCES `execution_status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `execution_status` (`id`, `name`) VALUES (1, 'STARTED');
INSERT INTO `execution_status` (`id`, `name`) VALUES (2, 'SUCCESS');
INSERT INTO `execution_status` (`id`, `name`) VALUES (3, 'ERROR');
