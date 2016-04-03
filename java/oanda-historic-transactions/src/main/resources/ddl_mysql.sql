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

create table oanda_transaction_result
(
transaction_id bigint(20) not null primary key,
max_adverse_price decimal(20,5) null,
is_bad_decision char(1) not null check(is_bad_decision in ('Y','N')),
constraint fk_tran_id foreign key(transaction_id) 
references oanda_transaction_history(transaction_id)
);

CREATE or replace VIEW `trade_data` 
AS 
select `t1`.`transaction_id` AS `transaction_id`,`t1`.`instrument` AS `instrument`,
`t1`.`units` AS `units`,`t1`.`price` AS `close_price`,`t1`.`pnl` AS `pnl`,
`t1`.`account_id` AS `account_id`,`t1`.`transaction_time` AS `transaction_close`,
`t2`.`price` AS `transaction_price`,`t2`.`transaction_time` AS `transaction_open`,
`t1`.`transaction_type` AS `transaction_type`,
timestampdiff(MINUTE,`t2`.`transaction_time`,`t1`.`transaction_time`) AS `duration_open`,
(case when (hour(`t1`.`transaction_time`) <= 6) then 'NIGHT' 
when (hour(`t1`.`transaction_time`) <= 14) then 'MORNING' 
when (hour(`t1`.`transaction_time`) <= 22) then 'EVENING' else 'NIGHT' end) AS `session`,
(case when (t1.pnl >=0) 
  then (case when `t1`.`price` > `t2`.`price` then 'LONG'
	   else 'SHORT' end)
  else  (case when `t1`.`price` > `t2`.`price` then 'SHORT'
	   else 'LONG' end) 
  end) as direction
from (`oanda_transaction_history` `t1` 
join `oanda_transaction_history` `t2` on((`t1`.`lnk_transaction_id` = `t2`.`transaction_id`))) 
where (`t1`.`transaction_type` in ('TRADE_CLOSE','STOP_LOSS_FILLED','TAKE_PROFIT_FILLED'));
