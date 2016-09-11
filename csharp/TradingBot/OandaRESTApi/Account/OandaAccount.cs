using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Account;
using Newtonsoft.Json;
namespace OandaRESTApi.Account
{
    public class OandaAccount : Account<long>
    {
        [JsonProperty("balance")]
        public override double TotalBalance { get; set; }
        [JsonProperty("unrealizedPl")]
        public override double UnRealisedPnl { get; set; }
        [JsonProperty("realizedPl")]
        public override double RealisedPnl { get; set; }
        [JsonProperty("marginUsed")]
        public override double MarginUsed { get; set; }
        [JsonProperty("marginAvail")]
        public override double MarginAvailable { get; set; }
        [JsonProperty("openTrades")]
        public override long OpenTrades { get; set; }
        [JsonProperty("accountCurrency")]
        public override string Currency { get; set; }
        [JsonProperty("accountId")]
        public override long Id { get; set; }
        [JsonProperty("marginRate")]
        public override double MarginRate { get; set; }
    }

    internal class OandaAccountSummary
    {
        [JsonProperty("accountId")]
        public  long Id { get; set; }

        [JsonProperty("marginRate")]
        public  double MarginRate { get; set; }

        [JsonProperty("accountCurrency")]
        public string Currency { get; set; }

        [JsonProperty("accountName")]
        public string Name { get; set; }
    }

    internal class OandaAccounts
    {
        [JsonProperty("accounts")]
        internal List<OandaAccount> Accounts { get; set; }
    }
}
