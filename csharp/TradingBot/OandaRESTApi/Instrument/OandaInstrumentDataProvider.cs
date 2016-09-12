using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Instrument;
using OandaRESTApi.Utils;
using TradingBotCore.Utils;
using TradingBotCore;
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
                string url = string.Format("{0}{1}?accountId={2}&fields={3}",
                            this.baseUrl, OandaConstants.INSTRUMENTS_RESOURCE, this.accountId, fieldsRequested);
                OandaTradeableInstruments instruments = OandaUtils.OandaJsonToObject<OandaTradeableInstruments>(url, this.accessToken);
                IEnumerable<TradeableInstrument<string>> instrsEn =  instruments.Instruments.Select(instr =>
                {
                    instr.InstrumentPairInterestRate = new InstrumentPairInterestRate();
                    string[] ccys = TradingUtils.splitCcyPair(instr.Instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
                    instr.InstrumentPairInterestRate.BaseCurrencyAskInterestRate = instr.BidAskInterestRate[ccys[0]].Ask;
                    instr.InstrumentPairInterestRate.BaseCurrencyBidInterestRate = instr.BidAskInterestRate[ccys[0]].Bid;
                    instr.InstrumentPairInterestRate.QuoteCurrencyAskInterestRate = instr.BidAskInterestRate[ccys[1]].Ask;
                    instr.InstrumentPairInterestRate.QuoteCurrencyBidInterestRate = instr.BidAskInterestRate[ccys[1]].Bid;
                    return instr;
                });
                return instrsEn.ToList();
            }
        }
    }
}
