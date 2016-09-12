using System.Collections.Generic;
using TradingBotCore.Position;
using Newtonsoft.Json;
namespace OandaRESTApi.Position
{
    internal class OandaPosition : Position<string> 
    {
        [JsonProperty("avgPrice")]
        public override double AveragePrice { get; set; }
        [JsonProperty("units")]
        public override long Units { get; set; }
        [JsonProperty("instrument")]
        public string CurrencyPair { get; set; }
        [JsonProperty("side")]
        public string BuyOrSell { get; set; }
    }

    internal class OandaPositions
    {
        [JsonProperty("positions")]
        public List<OandaPosition> Positions { get; set; }
    }
}
