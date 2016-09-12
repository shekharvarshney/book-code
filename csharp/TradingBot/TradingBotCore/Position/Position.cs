using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
namespace TradingBotCore.Position
{
    public class Position<T>
    {
        public virtual TradeableInstrument<T> Instrument { get; set; }
        public virtual long Units { get; set; }
        public virtual double AveragePrice { get; set; }
        public virtual TradingSignal Side { get; set; }
        public Position()
        {

        }
    }
}
