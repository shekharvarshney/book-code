using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Instrument
{
    public interface IInstrumentDataProvider<T>
    {
        ICollection<TradeableInstrument<T>> Instruments
        {
            get;
        }
    }
}
