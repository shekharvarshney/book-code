package com.precioustech.fxtrading.prediction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.precioustech.fxtrading.prediction.dao.PredictionDao;

public class NaiveBayesPredictionService {

	public static final String Y = "Y";
	public static final String N = "N";

	@Autowired
	private PredictionDao predictionDao;

	public double calculateNaiveBayes(String instrument, TradingSessionEnum session, DirectionEnum direction) {

		List<PredictionData> learningData = this.predictionDao.getLearningDataSet();
		Triple<Map<String, Map<String, Integer>>, Map<String, EnumMap<DirectionEnum, Integer>>, Map<String, EnumMap<TradingSessionEnum, Integer>>> allMaps = createMaps(
				learningData);
		double naiveClassifier = sessionAndBadDecisionProbability(N, session, allMaps.getRight())
				* instrumentAndBadDecisionProbability(N, instrument, allMaps.getLeft())
				* directionAndBadDecisionProbability(N, direction, allMaps.getMiddle())
				* badDecisionProbability(N, allMaps.getRight());
		double denominator = sessionProbability(session, allMaps.getRight())
				* instrumentProbability(instrument, allMaps.getLeft())
				* directionProbability(direction, allMaps.getMiddle());
		return naiveClassifier / denominator;
	}

	private double sessionAndBadDecisionProbability(String decision, TradingSessionEnum session,
			Map<String, EnumMap<TradingSessionEnum, Integer>> sessionData) {
		Integer total = sessionData.get(decision).values().stream().reduce((x, y) -> x + y).get();
		if(sessionData.get(decision).containsKey(session)) {
			return sessionData.get(decision).get(session) / (1.0 * total);
		} else  {
			return 0.0;
		}
	}

	private double instrumentAndBadDecisionProbability(String decision, String instrument,
			Map<String, Map<String, Integer>> instrumentData) {
		Integer total = instrumentData.get(decision).values().stream().reduce((x, y) -> x + y).get();
		if (instrumentData.get(decision).containsKey(instrument)) {
			return instrumentData.get(decision).get(instrument) / (1.0 * total);
		} else {
			return 0.0;
		}
	}

	private double directionAndBadDecisionProbability(String decision, DirectionEnum direction,
			Map<String, EnumMap<DirectionEnum, Integer>> directionData) {
		Integer total = directionData.get(decision).values().stream().reduce((x, y) -> x + y).get();
		if (directionData.get(decision).containsKey(direction)) {
			return directionData.get(decision).get(direction) / (1.0 * total);
		} else {
			return 0.0;
		}
	}

	private double badDecisionProbability(String decision,
			Map<String, EnumMap<TradingSessionEnum, Integer>> sessionData) {
		Integer total = sessionData.get(Y).values().stream().reduce((x, y) -> x + y).get()
				+ sessionData.get(N).values().stream().reduce((x, y) -> x + y).get();
		Integer ctr = sessionData.get(decision).values().stream().reduce((x, y) -> x + y).get();
		return ctr / (1.0 * total);
	}

	private double sessionProbability(TradingSessionEnum session,
			Map<String, EnumMap<TradingSessionEnum, Integer>> sessionData) {
		Integer total = sessionData.get(Y).values().stream().reduce((x, y) -> x + y).get()
				+ sessionData.get(N).values().stream().reduce((x, y) -> x + y).get();
		int Yctr = 0;
		int Nctr = 0;

		if (sessionData.get(Y).containsKey(session)) {
			Yctr = sessionData.get(Y).get(session);
		}

		if (sessionData.get(N).containsKey(session)) {
			Nctr = sessionData.get(N).get(session);
		}
		return (Yctr + Nctr) / (1.0 * total);
	}

	private double instrumentProbability(String instrument, Map<String, Map<String, Integer>> instrumentData) {
		Integer total = instrumentData.get(Y).values().stream().reduce((x, y) -> x + y).get()
				+ instrumentData.get(N).values().stream().reduce((x, y) -> x + y).get();
		int Yctr = 0;
		int Nctr = 0;

		if (instrumentData.get(Y).containsKey(instrument)) {
			Yctr = instrumentData.get(Y).get(instrument);
		}

		if (instrumentData.get(N).containsKey(instrument)) {
			Nctr = instrumentData.get(N).get(instrument);
		}
		return (Yctr + Nctr) / (1.0 * total);
	}

	private double directionProbability(DirectionEnum direction,
			Map<String, EnumMap<DirectionEnum, Integer>> directionData) {
		Integer total = directionData.get(Y).values().stream().reduce((x, y) -> x + y).get()
				+ directionData.get(N).values().stream().reduce((x, y) -> x + y).get();
		int Yctr = 0;
		int Nctr = 0;

		if (directionData.get(Y).containsKey(direction)) {
			Yctr = directionData.get(Y).get(direction);
		}

		if (directionData.get(N).containsKey(direction)) {
			Nctr = directionData.get(N).get(direction);
		}
		return (Yctr + Nctr) / (1.0 * total);
	}

	private Triple<Map<String, Map<String, Integer>>, Map<String, EnumMap<DirectionEnum, Integer>>, Map<String, EnumMap<TradingSessionEnum, Integer>>> createMaps(
			List<PredictionData> learningData) {
		Map<String, Map<String, Integer>> instrumentMap = Maps.newHashMap();
		instrumentMap.put(Y, Maps.newHashMap());
		instrumentMap.put(N, Maps.newHashMap());
		Map<String, EnumMap<DirectionEnum, Integer>> directionMap = Maps.newHashMap();
		directionMap.put(Y, Maps.newEnumMap(DirectionEnum.class));
		directionMap.put(N, Maps.newEnumMap(DirectionEnum.class));
		Map<String, EnumMap<TradingSessionEnum, Integer>> sessionMap = Maps.newHashMap();
		sessionMap.put(Y, Maps.newEnumMap(TradingSessionEnum.class));
		sessionMap.put(N, Maps.newEnumMap(TradingSessionEnum.class));

		for (PredictionData predictionData : learningData) {

			Map<String, Integer> instrumentInnerMap = instrumentMap.get(predictionData.getBadDecision());
			if (instrumentInnerMap.containsKey(predictionData.getInstrument())) {
				Integer currCount = instrumentInnerMap.get(predictionData.getInstrument());
				instrumentInnerMap.put(predictionData.getInstrument(), predictionData.getCount() + currCount);
			} else {
				instrumentInnerMap.put(predictionData.getInstrument(), predictionData.getCount());
			}

			EnumMap<DirectionEnum, Integer> directionInnerMap = directionMap.get(predictionData.getBadDecision());
			if (directionInnerMap.containsKey(predictionData.getDirectionEnum())) {
				Integer currCount = directionInnerMap.get(predictionData.getDirectionEnum());
				directionInnerMap.put(predictionData.getDirectionEnum(), predictionData.getCount() + currCount);
			} else {
				directionInnerMap.put(predictionData.getDirectionEnum(), predictionData.getCount());
			}

			EnumMap<TradingSessionEnum, Integer> sessionInnerMap = sessionMap.get(predictionData.getBadDecision());
			if (sessionInnerMap.containsKey(predictionData.getTradingSession())) {
				Integer currCount = sessionInnerMap.get(predictionData.getTradingSession());
				sessionInnerMap.put(predictionData.getTradingSession(), predictionData.getCount() + currCount);
			} else {
				sessionInnerMap.put(predictionData.getTradingSession(), predictionData.getCount());
			}
		}

		return new ImmutableTriple<Map<String, Map<String, Integer>>, Map<String, EnumMap<DirectionEnum, Integer>>, Map<String, EnumMap<TradingSessionEnum, Integer>>>(
				instrumentMap, directionMap, sessionMap);
	}

	// public static void main(String args[]) {
	// ApplicationContext appContext = new
	// ClassPathXmlApplicationContext("tradingbot-prediction-app.xml");
	// NaiveBayesPredictionService service =
	// appContext.getBean(NaiveBayesPredictionService.class);
	// // System.out.println(service.calculateNaiveBayes("EUR_NZD",
	// // TradingSessionEnum.MORNING, DirectionEnum.LONG));
	// }
}
