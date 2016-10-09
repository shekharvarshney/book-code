using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.MarketData.Historic;
namespace TradingBotCore.Utils
{
    public static class TradingUtils
    {
        public const int CCY_PAIR_LEN = 7;
        public const int CCY_SEP_CHR_POS = 3;
        private const int THOUSAND = 1000;
        public const string F2 = "F2";
        private static readonly DateTime epoch = new DateTime(1970, 1, 1); 
        public static DateTime fromUnixMicrosString(string unixstr)
        {
            long micros;
            if(long.TryParse(unixstr,out micros))
            {
                long ms = micros / 1000;
                return epoch.AddMilliseconds(ms);
            }
            else
            {
                throw new ArgumentException(string.Format("unable to parse {0} to a long value",unixstr));
            }
        }
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

        public static string getCandleStickLabel(CandleStickGranularity candleStickGranularity)
        {
            switch(candleStickGranularity)
            {
                case CandleStickGranularity.S5:
                    return "5 seconds";
                case CandleStickGranularity.S10:
                    return "10 seconds";
                case CandleStickGranularity.S15:
                    return "15 seconds";
                case CandleStickGranularity.S30:
                    return "30 seconds";
                case CandleStickGranularity.M1:
                    return "1 minute";
                case CandleStickGranularity.M2:
                    return "2 minutes";
                case CandleStickGranularity.M3:
                    return "3 minutes";
                case CandleStickGranularity.M5:
                    return "5 minutes";
                case CandleStickGranularity.M10:
                    return "10 minutes";
                case CandleStickGranularity.M15:
                    return "15 minutes";
                case CandleStickGranularity.M30:
                    return "30 minutes";
                case CandleStickGranularity.H1:
                    return "1 hour";
                case CandleStickGranularity.H2:
                    return "2 hours";
                case CandleStickGranularity.H3:
                    return "3 hours";
                case CandleStickGranularity.H4:
                    return "4 hours";
                case CandleStickGranularity.H6:
                    return "6 hours";
                case CandleStickGranularity.H8:
                    return "8 hours";
                case CandleStickGranularity.H12:
                    return "12 hours";
                case CandleStickGranularity.D:
                    return "1 day";
                case CandleStickGranularity.W:
                    return "1 week";
                case CandleStickGranularity.M:
                    return "1 month";
            }
            return string.Empty;
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
