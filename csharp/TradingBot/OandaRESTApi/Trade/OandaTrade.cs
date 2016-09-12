using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Trade;
using Newtonsoft.Json;
namespace OandaRESTApi.Trade
{
    internal class OandaTrade : Trade<long,string,long> 
    {
        [JsonProperty("units")]
        public override long Units { get; set; }
        [JsonProperty("side")]
        public string BuyOrSell { get; set; }
        [JsonProperty("instrument")]
        public string CurrencyPair { get; set; }
        [JsonProperty("id")]
        public override long TradeId { get; set; }
        [JsonProperty("takeProfit")]
        public override double TakeProfitPrice { get; set; }
        [JsonProperty("price")]
        public override double ExecutionPrice { get; set; }
        [JsonProperty("stopLoss")]
        public override double StopLoss { get; set; }
        [JsonProperty("time")]
        public string TradeDateString { get; set; }
    }
}
