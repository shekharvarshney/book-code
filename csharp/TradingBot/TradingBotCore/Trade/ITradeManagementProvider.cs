using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Trade
{
    public interface ITradeManagementProvider<M,N,K>
    {
        bool modifyTrade(K accountId, M tradeId, double stopLoss, double takeProfit);

        bool closeTrade(M tradeId, K accountId);

        Trade<M, N, K> getTradeForAccount(M tradeId, K accountId);

        ICollection<Trade<M, N, K>> getTradesForAccount(K accountId);
    }
}
