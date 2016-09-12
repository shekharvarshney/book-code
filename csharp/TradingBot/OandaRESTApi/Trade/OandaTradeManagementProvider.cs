using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Trade;

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

        public Trade<long, string, long> getTradeForAccount(long tradeId, long accountId)
        {
            throw new NotImplementedException();
        }

        public ICollection<Trade<long, string, long>> getTradesForAccount(long accountId)
        {
            throw new NotImplementedException();
        }

        public bool modifyTrade(long accountId, long tradeId, double stopLoss, double takeProfit)
        {
            throw new NotImplementedException();
        }
    }
}
