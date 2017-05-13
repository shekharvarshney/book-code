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

# Introduction to Trading Bot

Welcome to the world of automated trading! The fact that you are reading this book, suggests that you want to probably build your own bot which hopefully can make you some money whilst you are busy with your day job or like me want to experiment with the technology that goes into building such a bot using Java.

Automated trading has been around for a while, although it has largely been a preserve of big players such as banks and hedge funds.

This has however changed in the last few years. With many retail investors, able to trade on various platforms and exchanges directly, instead of using the services of a traditional broker on the phone, the demand has been growing to automate the task of placing orders, whilst these investors get on with their day jobs. As a first step in automating this process, many platforms such as OANDA, LMAX etc. provide APIs for various programming languages such as Java, Python, C#, PHP etc. so that the mundane tasks of watching the market, looking at charts, doing analysis can be automated.

On this journey, we will focus not only on the concepts of automated trading, but also on writing clean, test driven Java programs.

Towards the end, we would not only have a working trading bot, that would be ready to trade with any strategy but from a technical perspective, we would have also gained an appreciation into the event-driven, multithreaded world of java programming.

***Warning:*** *Trading foreign exchange on margin carries a high level of risk, and may not be suitable for all investors. Past performance is not indicative of future results. The high degree of leverage can work against you as well as for you. Before deciding to invest in foreign exchange you should carefully consider your investment objectives, level of experience, and risk appetite. The possibility exists that you could sustain a loss of some or all of your initial investment and therefore you should not invest money that you cannot afford to lose. You should be aware of all the risks associated with foreign exchange trading, and seek advice from an independent financial advisor if you have any doubts.*

## What is a Trading Bot?

In very simple language, a *trading bot*, is a computer program, that can automatically place orders to a market or exchange, without the need for human intervention. The simplest of bots could be a curl[^curl] *POST* to an OANDA REST API, such as

[^curl]: https://en.wikipedia.org/wiki/CURL

```perl
$curl -X POST -d "instrument=EUR_USD&units=2&side=sell&type=market" "https://api-fxtrade.oanda.com/v1/accounts/12345/orders"
```
which can be setup on a unix cron[^cron] to run every hour, during trading hours. It has no strategy, nor any external interface or dependencies. It is a one liner to place an order which has an equal probability to be in profit or in loss.

On the other end of the spectrum, it could be a complex program based on a distributed architecture, consuming lots of feeds from various sources, analysing them in realtime and then placing an order. It would be highly available with extremely low latency.

[^cron]: https://en.wikipedia.org/wiki/Cron

The scale and scope of the bot, as we can see is varied. To be effective, the bot should be able to accomplish the following tasks:

- Consume market data and/or ,external news events, social media feeds  and distribute to interested components within the system.
- Have atleast one strategy which provides a trading signal.
- Based on a trading signal place  Orders with the brokerage platform.
- Account management, i.e., have the ability to keep track of margin requirements, leverage, PNL, amount remaining etc. in order to curb trading if the amount available breaches a given threshold.
- Position Management i.e. keep track of all currently active positions of various instruments, units of such positions, average price etc.
- Have the ability to handle events which are triggered by the brokerage platform such as ORDER_FILLED, STOP_LOSS etc. and if required take appropriate decisions for such events.
- Some basic monitoring and alerting.
- Some basic risk management. For e.g. loss limitation by using stop losses for orders or making sure that risk is distributed between risky and safe haven instruments. These are just examples and by no means a comprehensive list of fully managing the risk.

## Why do we need a Trading Bot?

I believe most of services provided by exchanges/platforms revolve around the following:

- Market data subscription for instruments of choice and dissemination.
- Place orders and trades.
- Account and position management.
- Historic market data.
- Heartbeating.
- Callbacks for trade, order and account events.
- Authentication

The trading bot is an attempt to generalise these tasks in a framework and provide an ability to provide the broker/exchange platform specific implementation at run time, using a dependency injection[^di] framework like Spring. Therefore, theoretically speaking, it would just be a change in the Spring configuration file, where we define our implementations for various interfaces that implement these services, and la voila, we should be able to support various broker/exchange platforms.

[^di]: https://en.wikipedia.org/wiki/Dependency_injection

## The capabilities of our Trading Bot

Our bot would have the following capabilities which would be discussed in detail, in later chapters:

- Account Management
- Integration with realtime market data feed
- Dissemination of market data
- Place orders
- Handle order/trade and account events
- Analysis of historic prices
- Integration with Twitter
- Strategies

## Design Goals

- One of the key design goals, alluded to in the beginning of this chapter, is to have the ability to change the implementation of a broker/exchange platform at runtime through Spring configuration. This is possible, if we can create specifications for these platform API calls, very similar to the JDBC specification. For e.g. a sample specification/interface defining the position management requirements are

```java
/**
 * A provider of services for instrument positions. A position for an instrument
 * is by definition aggregated trades for the instrument with an average price
 * where all trades must all be a LONG or a SHORT. It is a useful service to
 * project a summary of a given instrument and also if required close all trades
 * for a given instrument, ideally using a single call.
 *
 * The implementation might choose to maintain an internal cache of positions in
 * order to reduce latency. If this is the case then it must find means to
 * either 1) hook into the event streaming and refresh the cache based on an
 * order/trade event or 2) regularly refresh the cache after a given time
 * period.
 *
 * @param <M>
 *      The type of instrumentId in class TradeableInstrument
 * @param <N>
 *      the type of accountId
 *
 * @see TradeableInstrument
 */
public interface PositionManagementProvider<M, N> {

	/**
	 *
	 * @param accountId
	 * @param instrument
	 * @return Position<M> for a given instrument and accountId(may be null if
	 *         all trades under a single account).
	 */
	Position<M> getPositionForInstrument(N accountId, TradeableInstrument<M> instrument);

	/**
	 *
	 * @param accountId
	 * @return Collection of Position<M> objects for a given accountId.
	 */
	Collection<Position<M>> getPositionsForAccount(N accountId);

	/**
	 * close the position for a given instrument and accountId. This is one shot
	 * way to close all trades for a given instrument in an account.
	 *
	 * @param accountId
	 * @param instrument
	 * @return if the operation was successful
	 */
	boolean closePosition(N accountId, TradeableInstrument<M> instrument);

}
```

If we create such specifications/interfaces for each aspect of the platform interaction, we can in theory create providers for these services and swap them as when required, through the Spring configuration. From code organisation perspective, all these interfaces, therefore would go in a project, that form part of the core api. This project would therefore be broker/exchange provider agnostic and would comprise such interfaces and services.

- Write services that solve a single business problem or a collection of related problems. These services lend themselves to easy unit testability and code reuse that eventually leads to better software quality.
- Loosely couple services. This enables reducing system dependencies and as a result results in more maintainable software. Our software would be continuously evolving as one might decide to integrate more social media feeds or add more complex strategies. Writing loosley coupled components ensures that we have little knock on effect on already working code.
- High unit test coverage. It is extremely important that we aim to have a high unit test coverage. When used in a production environment where real money could be involved, a large unit tests coverage will ensure that we catch regressions and bugs early on and prevent the manifestation of bugs as much as possible in the production environment.

## Code Organisation and Software Stack Used

Following on from our discussion of design goals in the previous sections, the code will be organised in atleast 3 different projects ,i.e., atleast 3 jar files will be produced from the build. Why are we saying atleast 3? Remember from our earlier discussion, that one of the key design goals is to be able to switch provider implementation at runtime. Since we could have more than 1 provider from which we can decide, there will be atleast 3 jar files. We are going to discuss only 1 implementation in the book,i.e., the OANDA REST API implementation. Developers who would use the framework, are encouraged to develop more provider implementations.

- ***trading-core*** is the core of the project. It comprises all the specifications/interfaces that must be implemented. It also comprises all the generic services which make use of the core interfaces and provide additional useful API methods.
- ***oanda-restapi*** is our reference implementation for the specification and will be discussed in the book. You are more than welcome to swap this over with your own.
- ***tradingbot-app*** is the main application that uses Spring to inject the provider api at runtime. It is also the project where we define our strategies and can implement app specific stuff. Later in the book, we are going to talk about integration with social media, especially *Twitter*, which we would implement in this project.


To build our bot we are going to use the following set of software and tools

- Java SDK 1.7+
- Spring Framework 4.1, Spring Social 1.1 (Dependency in *tradingbot-app* project only)
- Guava 18.0
- HttpClient 4.3
- Maven 3.2.5
- Eclipse IDE

## OANDA REST API as Reference Implementation

**Declaration-** *I have no current or past commercial relationship with OANDA and I have chosen OANDA REST API as a reference implementation simply on it's technical merit and the fact that it is likely to have wider adoption as it is free to use. If there is something similar around, I would be happy to give it a try as well.*

I encountered the OANDA API by chance. At the time, I was looking for a broker who was offering free API for trading (just in case i wanted to convert to a production account) and more importantly supported the Java programming language.
It was very easy to get started, as most of the examples used *curl* to demonstrate various trading actions like getting a list of instruments, place an order or get historical candle sticks data. I could just type the *curl* commands on my mac terminal and fire away. I could easily see the json response received with each *curl* request fired. By seeing the responses and data in realtime, I got a really good idea and appreciation of various APIs supporting instruments, orders, rates, positions etc.

The *curl* command examples was a good starting point and I started to experiment writing equivalent commands as test cases in Java described on the REST API[^rapi] page.

[^rapi]: http://developer.oanda.com/rest-live/introduction/
