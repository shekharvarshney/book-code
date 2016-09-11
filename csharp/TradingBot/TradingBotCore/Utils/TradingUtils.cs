using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Utils
{
    public static class TradingUtils
    {
        public const int CCY_PAIR_LEN = 7;
        public const int CCY_SEP_CHR_POS = 3;
        private const int THOUSAND = 1000;
        public const string F2 = "F2";
        public static string[] splitCcyPair(string instrument, char currencySeparator)
        {
            if(!string.IsNullOrEmpty(instrument))
            {
                return instrument.Split(currencySeparator);
            }
            else
            {
                throw new ArgumentException("instrument cannot be null or empty");
            }    
        }

        public static TradingSignal flip(TradingSignal signal)
        {
            switch(signal)
            {
                case TradingSignal.LONG:
                    return TradingSignal.SHORT;
                case TradingSignal.SHORT:
                    return TradingSignal.LONG;
                default:
                    return TradingSignal.NONE;
            }
        }
    }
}
