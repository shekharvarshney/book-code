using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
namespace OandaRESTApi.Instrument
{
    public class OandaInstrumentDataProvider : IInstrumentDataProvider<string>
    {
        private readonly string baseUrl;
        private readonly long accountId;
        private readonly string accessToken;
        internal const string fieldsRequested = "instrument%2Cpip%2CinterestRate";
        public OandaInstrumentDataProvider(string baseUrl, long accountId, string accessToken)
        {
            this.baseUrl = baseUrl;
            this.accountId = accountId;
            this.accessToken = accessToken;
        }

        internal string InstrumentsUrl
        {
            get
            {
                return string.Format("{0}{1}?accountId={2}&fields={3}", 
                    this.baseUrl, OandaConstants.INSTRUMENTS_RESOURCE, this.accountId, fieldsRequested);
            }
        }

        public ICollection<TradeableInstrument<string>> Instruments
        {
            get
            {
                throw new NotImplementedException();
            }
        }
    }
}
