using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
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
        public IDictionary<string, OandaBidAskInterestRate> BidAskInterestRate;
    }

    internal class OandaBidAskInterestRate
    {
        [JsonProperty("bid")]
        public double Bid;
        [JsonProperty("ask")]
        public double Ask;
    }

    internal class OandaTradeableInstruments
    {
        [JsonProperty("instruments")]
        public List<OandaTradeableInstrument> instruments;
    }
}
