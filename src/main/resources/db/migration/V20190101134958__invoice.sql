CREATE TABLE `invoice` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME NOT NULL,

    `invoice_id` VARCHAR(64) NOT NULL,
    `tenant_realm` varchar(64) NULL,
    `keycloak_id` VARCHAR(64) NOT NULL,

    `period_start` DATETIME NOT NULL,
    `period_end` DATETIME NOT NULL,
    `value` float(18,2) NOT NULL,

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `invoice_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME NOT NULL,
    `invoice_id` BIGINT(20) NOT NULL,

    `hits` BIGINT(20) NOT NULL,
    `charged_amount` float(18,6) NOT NULL,
    `endpoint` varchar(128) NULL,

    `subscription_uuid` varchar(64) NULL,
    `plan_uuid` varchar(64) NULL,
    `api_uuid` varchar(64) NULL,
    `provider_uuid` varchar(128) NULL,
    `gateway_id` varchar(64) NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `FK_invoice_item___invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;