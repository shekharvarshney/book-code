using Newtonsoft.Json;
using System.Collections.Generic;
using TradingBotCore.Instrument;
namespace OandaRESTApi.Instrument
{
    internal class OandaTradeableInstrument : TradeableInstrument<string>
    {
        [JsonProperty("pip")]
        public override double Pip { get; set; }
        [JsonProperty("instrument")]
        public override string Instrument { get; set; }
        [JsonProperty("interestRate")]
        public IDictionary<string, OandaBidAskInterestRate> BidAskInterestRate { get; set; }
    }

    internal class OandaBidAskInterestRate
    {
        [JsonProperty("bid")]
        public double Bid { get; set; }
        [JsonProperty("ask")]
        public double Ask { get; set; }
    }

    internal class OandaTradeableInstruments
    {
        [JsonProperty("instruments")]
        public List<OandaTradeableInstrument> Instruments { get; set; }
    }
}
