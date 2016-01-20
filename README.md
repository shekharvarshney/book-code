# Automated Trading Bot Framework

The software is covered by the *Apache License* and is discussed in my book [Developing a Trading Bot using JAVA](http://www.leanpub.com/tradingbot)

##How to run the software

1. Download the source code.
2. Change the **tradingbot.properties** by configuring the email and twitter properties
3. Change the **tradingbot-oanda.properties** by configuring the oanda specific properties
4. Build the 3 projects using maven in the order **tradingbot-core**, **oanda-restapi** and **tradingbot-core**.
5. use mvn:exec to launch FXTradingBot main class with runtime argument *tradingbot-oanda.xml*
