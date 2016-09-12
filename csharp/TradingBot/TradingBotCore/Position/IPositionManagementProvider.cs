using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
namespace TradingBotCore.Position
{
    public interface IPositionManagementProvider<M,N>
    {
        Position<M> getPositionForInstrument(N accountId, TradeableInstrument<M> instrument);
        ICollection<Position<M>> getPositionsForAccount(N accountId);
        bool closePosition(N accountId, TradeableInstrument<M> instrument);
    }
}
