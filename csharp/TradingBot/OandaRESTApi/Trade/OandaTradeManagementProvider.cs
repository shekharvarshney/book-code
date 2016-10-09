using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Trade;
using TradingBotCore;
using OandaRESTApi.Utils;
using TradingBotCore.Utils;
using TradingBotCore.Instrument;
using System.Net;
namespace OandaRESTApi.Trade
{
    public class OandaTradeManagementProvider : ITradeManagementProvider<long, string, long>
    {
        private readonly string baseUrl;
        private readonly string token;
        private const string tradesResource = "/trades";
        public OandaTradeManagementProvider(string baseUrl, string accessToken)
        {
            this.baseUrl = baseUrl;
            this.token = accessToken;
        }   

        public bool closeTrade(long tradeId, long accountId)
        {
            throw new NotImplementedException();
        }

        internal string getTradeForAccountUrl(long tradeId, long accountId)
        {
            return this.baseUrl + OandaConstants.ACCOUNTS_RESOURCE +
                TradingConstants.FWD_SLASH + accountId + tradesResource
                + TradingConstants.FWD_SLASH + tradeId;
        }

        public Trade<long, string, long> getTradeForAccount(long tradeId, long accountId)
        {
            string tradeForAccUrl = getTradeForAccountUrl(tradeId, accountId);
            OandaTrade trade = OandaUtils.OandaJsonToObject<OandaTrade>(tradeForAccUrl, this.token);
            setTradeValues(trade);
            return trade;
        }

        private void setTradeValues(OandaTrade trade)
        {
            trade.Instrument = new TradeableInstrument<string>(trade.CurrencyPair);
            trade.Side = OandaUtils.toTradingSignal(trade.BuyOrSell);
            trade.TradeDate = TradingUtils.fromUnixMicrosString(trade.TradeDateString);
        }

        public ICollection<Trade<long, string, long>> getTradesForAccount(long accountId)
        {
            string tradesInfoUrl = this.baseUrl + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + tradesResource;
            OandaTrades tradesForAccount = OandaUtils.OandaJsonToObject<OandaTrades>(tradesInfoUrl, this.token);
            IEnumerable<Trade<long, string, long>> tradeEn = tradesForAccount.Trades.Select(trade =>
            {
                setTradeValues(trade);
                return trade;
            });
            return tradeEn.ToList();
        }

        public bool modifyTrade(long accountId, long tradeId, double stopLoss, double takeProfit)
        {
            HttpWebRequest patchRequest = HttpWebRequest.Create(getTradeForAccountUrl(tradeId,accountId)) as HttpWebRequest;
            patchRequest.Method = RESTVerbs.PATCH.ToString();
            KeyValuePair<string, string> oandaAuthHdr = OandaUtils.OandaAuthHeader(this.token);
            patchRequest.Headers.Add(oandaAuthHdr.Key, oandaAuthHdr.Value);
            
            throw new NotImplementedException();
        }
    }
}
