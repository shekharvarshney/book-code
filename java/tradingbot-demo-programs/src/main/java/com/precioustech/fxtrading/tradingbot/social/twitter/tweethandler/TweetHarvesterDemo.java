package com.precioustech.fxtrading.tradingbot.social.twitter.tweethandler;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.tradingbot.social.twitter.CloseFXTradeTweet;
import com.precioustech.fxtrading.tradingbot.social.twitter.NewFXTradeTweet;

public class TweetHarvesterDemo {
	private static final Logger LOG = Logger.getLogger(TweetHarvesterDemo.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		ApplicationContext appContext = new ClassPathXmlApplicationContext("tweetharvester-demo.xml");
		TweetHarvester<String> tweetHarvester = appContext.getBean(TweetHarvester.class);

		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
		String userId = "Forex_EA4U";
		// String userId = "SignalFactory";
		final int tweetsToDump = 10;
		int ctr = 0;

		Collection<NewFXTradeTweet<String>> newTradeTweets = tweetHarvester.harvestNewTradeTweets(userId);
		LOG.info(String.format("+++++++++ Dumping the first %d of %d new fx tweets for userid %s +++++++",
				tweetsToDump, newTradeTweets.size(), userId));
		Iterator<NewFXTradeTweet<String>> newTweetItr = newTradeTweets.iterator();
		while (newTweetItr.hasNext() && ctr < tweetsToDump) {
			NewFXTradeTweet<String> newFxTweet = newTweetItr.next();
			LOG.info(newFxTweet);
			ctr++;
		}

		Collection<CloseFXTradeTweet<String>> closedTradeTweets = tweetHarvester.harvestHistoricTradeTweets(userId,
				eurusd);
		ctr = 0;
		Iterator<CloseFXTradeTweet<String>> closedTweetItr = closedTradeTweets.iterator();
		LOG.info(String.format("+++++++++ Dumping the first %d of %d closed fx tweets for userid %s +++++++",
				tweetsToDump, closedTradeTweets.size(), userId));
		while (closedTweetItr.hasNext() && ctr < tweetsToDump) {
			CloseFXTradeTweet<String> closeFxTweet = closedTweetItr.next();
			LOG.info(String.format("Instrument %s, profit = %3.1f, price=%2.5f ", closeFxTweet.getInstrument()
					.getInstrument(), closeFxTweet.getProfit(), closeFxTweet.getPrice()));
			ctr++;
		}

	}

}
