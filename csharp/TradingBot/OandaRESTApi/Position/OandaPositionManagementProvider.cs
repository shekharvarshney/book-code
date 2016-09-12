using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
using TradingBotCore.Position;
using TradingBotCore;
using OandaRESTApi.Utils;
namespace OandaRESTApi.Position
{
    public class OandaPositionManagementProvider : IPositionManagementProvider<string, long>
    {
        private readonly string baseUrl;
        private readonly string token;
        private const string positionsResource = "/positions";
        public OandaPositionManagementProvider(string baseUrl, string accessToken)
        {
            this.baseUrl = baseUrl;
            this.token = accessToken;
        }

        public bool closePosition(long accountId, TradeableInstrument<string> instrument)
        {
            throw new NotImplementedException();
        }

        public ICollection<Position<string>> getPositionsForAccount(long accountId)
        {
            string positionUrl = this.baseUrl + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + 
                accountId + positionsResource;
            OandaPositions positions = OandaUtils.OandaJsonToObject<OandaPositions>(positionUrl, this.token);
            IEnumerable<Position<string>> posEn =  positions.Positions.Select(pos =>
            {
                pos.Instrument = new TradeableInstrument<string>(pos.CurrencyPair);
                pos.Side = OandaUtils.toTradingSignal(pos.BuyOrSell);
                return pos;
            });
            
            return posEn.ToList();
            
        }

        internal string getPositionForInstrumentUrl(long accountId, TradeableInstrument<string> instrument)
        {
            return this.baseUrl + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + positionsResource
                + TradingConstants.FWD_SLASH + instrument.Instrument;
        }

        public Position<string> getPositionForInstrument(long accountId, TradeableInstrument<string> instrument)
        {
            return OandaUtils.OandaJsonToObject<Position<string>>(getPositionForInstrumentUrl(accountId, instrument), this.token);
        }
    }
}
