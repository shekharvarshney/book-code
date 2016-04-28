#!/bin/bash

cd  /home/shekhar/code-repo/book-code/java/oanda-historic-transactions
mvn exec:java -Dexec.mainClass="com.precioustech.fxtrading.oanda.restapi.account.transaction.OandaTransactionService"

mvn exec:java -Dexec.mainClass="com.precioustech.fxtrading.oanda.restapi.account.transaction.AnalyseTransactionResultService"
