# Automated Trading Bot Framework

The software is covered by the *Apache License* and is discussed in my book [Developing a Trading Bot using JAVA](http://www.apress.com/us/book/9781484225196)

## How to run the software

1. Download the source code.
2. Change the **tradingbot.properties** by configuring the email and twitter properties
3. Change the **tradingbot-oanda.properties** by configuring the your oanda account specific properties
4. Build the bot by executing script **buildbot.bsh**. The script uses maven. Please make sure its properly configured.
5. Run the bot using script **runbot-oanda.bsh**

Follow live tweets of trades executed by the bot at [Java TradingBot Twitter Feed](https://twitter.com/javatradingbot).

The bot is currently using the strategies discussed in the book and some experimental strategies which are implemented in the branch **new_ideas**.
