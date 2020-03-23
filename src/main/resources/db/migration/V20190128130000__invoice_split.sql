CREATE TABLE `invoice_split` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,

  `invoice_id` bigint(20) NOT NULL,

  `tenant_value` decimal(18,6) NULL,
  `provider_value` decimal(18,6) NULL,

  `api_uuid` varchar(64) NOT NULL,
  `provider_uuid` varchar(64) NOT NULL,
  `plan_uuid` varchar(64) NOT NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT `FK_invoice_split___invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;