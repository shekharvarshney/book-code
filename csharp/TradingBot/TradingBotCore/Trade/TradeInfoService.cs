using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Account;
using System.Collections.Concurrent;
using TradingBotCore.Instrument;
namespace TradingBotCore.Trade
{
    public class TradeInfoService<M,N,K>
    {
        private readonly ITradeManagementProvider<M, N, K> tradeManagementProvider;
        private readonly IAccountProvider<K> accountDataProvider;
        private readonly ConcurrentDictionary<K, IDictionary<TradeableInstrument<N>, ICollection<Trade<M, N, K>>>> tradesCache
            = new ConcurrentDictionary<K, IDictionary<TradeableInstrument<N>, ICollection<Trade<M, N, K>>>>();
        public TradeInfoService(ITradeManagementProvider<M,N,K> tradeManagementProvider, 
            IAccountProvider<K> accountDataProvider)
        {
            this.tradeManagementProvider = tradeManagementProvider;
            this.accountDataProvider = accountDataProvider;
        }
    }
}
