package com.precioustech.fxtrading.prediction.dao;

import java.util.List;

import com.precioustech.fxtrading.prediction.PredictionData;

public interface PredictionDao {

	List<PredictionData> getLearningDataSet();
}
