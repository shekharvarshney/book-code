create table currency_events
(
  currency_event_id int not null auto_increment primary key,
  currency_event_date datetime,
  currency varchar(5),
  currency_event_descr varchar(512),
  importance varchar(10),
  actual varchar(32),
  forecast varchar(32),
  previous varchar(32)
);