using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
namespace TradingBotCore.Trade
{
    public class Trade<M,N,K>
    {
        public virtual M TradeId { get; set; }
        public virtual long Units { get; set; }
        public TradingSignal Side { get; set; }
        public  TradeableInstrument<N> Instrument { get; set; }
        public virtual DateTime TradeDate { get; set; }
        public virtual double TakeProfitPrice { get; set; }
        public virtual double ExecutionPrice { get; set; }
        public virtual double StopLoss { get; set; }
        public virtual K AccountId { get; set; }
    }
}
