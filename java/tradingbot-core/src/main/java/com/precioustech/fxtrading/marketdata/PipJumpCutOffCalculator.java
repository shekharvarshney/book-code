package com.precioustech.fxtrading.marketdata;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public interface PipJumpCutOffCalculator<T> {

	Double calculatePipJumpCutOff(TradeableInstrument<T> instrument);
}
