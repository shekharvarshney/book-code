using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Instrument
{
    public class InstrumentPairInterestRate
    {
        public InstrumentPairInterestRate():this(default(double), default(double), default(double), default(double))
        {

        }

        public InstrumentPairInterestRate(double baseCurrencyBidInterestRate, double baseCurrencyAskInterestRate,
            double quoteCurrencyBidInterestRate, double quoteCurrencyAskInterestRate)
        {
            BaseCurrencyBidInterestRate = baseCurrencyBidInterestRate;
            BaseCurrencyAskInterestRate = baseCurrencyAskInterestRate;
            QuoteCurrencyBidInterestRate = quoteCurrencyBidInterestRate;
            QuoteCurrencyAskInterestRate = quoteCurrencyAskInterestRate;
        }
        public double BaseCurrencyBidInterestRate { get; set; }
        public double BaseCurrencyAskInterestRate { get; set; }
        public double QuoteCurrencyBidInterestRate { get; set; }
        public double QuoteCurrencyAskInterestRate { get; set; }
    }
}
