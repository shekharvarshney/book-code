CREATE TABLE `oanda_account` (
  `account_id` int(11) NOT NULL,
  `currency` char(3) NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `oanda_transaction_history` (
  `transaction_id` bigint(20) NOT NULL,
  `transaction_type` varchar(128) NOT NULL,
  `account_id` int(11) NOT NULL,
  `instrument` char(7) NOT NULL,
  `units` int(11) NOT NULL,
  `transaction_time` datetime NOT NULL,
  `price` decimal(20,5) NOT NULL,
  `interest` decimal(10,5) DEFAULT NULL,
  `pnl` decimal(10,5) DEFAULT NULL,
  `lnk_transaction_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `fk_oanda_acc_id` (`account_id`),
  CONSTRAINT `fk_oanda_acc_id` FOREIGN KEY (`account_id`) REFERENCES `oanda_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;