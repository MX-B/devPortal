CREATE TABLE `execution_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `uuid` varchar(42) NOT NULL,
    `created_at` datetime NOT NULL,
    `updated_at` datetime NULL,
    `removed_at` datetime NULL,

    `invoice_id` BIGINT(20) NULL,
    `execution_id` BIGINT(20) NOT NULL,
    `status_id` int(3) NOT NULL,
    `tenant_realm` VARCHAR(64) NOT NULL,
    `keycloak_id` VARCHAR(64) NOT NULL,
    `error_reason` VARCHAR(1024),

    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_uuid___execution_user` (`uuid`),
    CONSTRAINT `FK_execution_user___status` FOREIGN KEY (`status_id`) REFERENCES `execution_status` (`id`),
    CONSTRAINT `FK_execution_user___execution` FOREIGN KEY (`execution_id`) REFERENCES `execution` (`id`),
    CONSTRAINT `FK_execution_user___invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;