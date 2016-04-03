update oanda_transaction_history set transaction_type='DAILY_INTEREST' 
where transaction_type='Interest' and transaction_id>0;

update oanda_transaction_history set transaction_type='FEE' 
where transaction_type in ("Fund Fee", "Wire Fee") and transaction_id>0;

update oanda_transaction_history set transaction_type='TRANSFER_FUNDS' 
where transaction_type in ("Fund Withdrawal (Transfer)",
			"Fund Withdrawal (Account Transfer)",
			"Fund Deposit (Transfer)",
			"Fund Deposit (Account Transfer)") and transaction_id>0;

update oanda_transaction_history set transaction_type='SET_MARGIN_RATE' 
where transaction_type in ("Change Margin") and transaction_id>0;

update oanda_transaction_history set transaction_type='MARGIN_CALL_ENTER' 
where transaction_type in ("Margin Alert Entered") and transaction_id>0;

update oanda_transaction_history set transaction_type='ORDER_FILLED' 
where transaction_type in ("Std Limit Order Filled",
			"Sell Market Filled",
			"Sell Limit Filled",
			"Order Filled",
			"Buy Market Filled",
			"Buy Limit Filled") and transaction_id>0;


update oanda_transaction_history set transaction_type='ORDER_CANCEL' 
where transaction_type in ("Order Cancelled", "Order Cancelled (BV:SL)") and transaction_id>0;

update oanda_transaction_history set transaction_type='ORDER_UPDATE' 
where transaction_type in ("Change Order") and transaction_id>0;

update oanda_transaction_history set transaction_type='LIMIT_ORDER_CREATE' 
where transaction_type in ("Sell Limit Order", "Buy Limit Order") and transaction_id>0;

update oanda_transaction_history set transaction_type='MARKET_ORDER_CREATE' 
where transaction_type in ("Buy Market", "Sell Market") and transaction_id>0;

update oanda_transaction_history set transaction_type='TRADE_UPDATE' 
where transaction_type in ("Change Trade") and transaction_id>0;

update oanda_transaction_history set transaction_type='TRADE_CLOSE' 
where transaction_type in ("Close Trade") and transaction_id>0;

update oanda_transaction_history set transaction_type='STOP_LOSS_FILLED' 
where transaction_type in ("Stop Loss") and transaction_id>0;

update oanda_transaction_history set transaction_type='TAKE_PROFIT_FILLED' 
where transaction_type in ("Take Profit") and transaction_id>0;

update oanda_transaction_history set transaction_type='TRAILING_STOP_FILLED' 
where transaction_type in ("Trailing Stop") and transaction_id>0;

update oanda_transaction_history set transaction_type='TRANSFER_FUNDS' 
where transaction_type in ("Fund Withdrawal","Fund Deposit") and transaction_id>0;

update oanda_transaction_history set transaction_type='MARKET_ORDER_CREATE' 
where transaction_type in ('Buy Order','Sell Order') and transaction_id>0;

update oanda_transaction_history set transaction_type='ORDER_CANCEL' 
where transaction_type in ('Order Expired') and transaction_id>0;

update oanda_transaction_history
set instrument=replace(instrument,"/","_")
where substring(instrument,4,1)='/' and transaction_id>0;
