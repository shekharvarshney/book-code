using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Immutable;
namespace TradingBotCore.Instrument
{
    public class InstrumentService<T>
    {
        public readonly IInstrumentDataProvider<T> instrumentProvider;
        private readonly IDictionary<string, TradeableInstrument<T>> instrumentMap;
        public InstrumentService(IInstrumentDataProvider<T> instrumentProvider)
        {
            this.instrumentProvider = instrumentProvider;
            var builder = ImmutableDictionary.CreateBuilder<string, TradeableInstrument<T>>();
            instrumentProvider.Instruments.AsEnumerable().ToList().ForEach(instr => builder.Add(instr.Instrument,instr));
            instrumentMap = builder.ToImmutable();
        }

        public ICollection<TradeableInstrument<T>> AllInstruments
        {
            get
            {
                return instrumentMap.Values;
            }
        }

        public ICollection<TradeableInstrument<T>> getAllPairsWithCurrency(string currency)
        {
            return instrumentMap.Where(kvpair => kvpair.Key.Contains(currency)).Select(v=>v.Value).ToList();
        }

        public double getPipForInstrument(TradeableInstrument<T> instrument)
        {
            if(instrument == null)
            {
                throw new ArgumentException("instrument cannot be null");
            }
            else
            {
                return this.instrumentMap.ContainsKey(instrument.Instrument) ? 
                    this.instrumentMap[instrument.Instrument].Pip : 1.0;
            }
        }
    }
}
