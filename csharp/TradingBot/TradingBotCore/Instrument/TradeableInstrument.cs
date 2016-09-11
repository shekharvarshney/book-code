using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Instrument
{
    public class TradeableInstrument<T>
    {
        public virtual string Instrument { get; set; }
        public virtual string Description { get; set; }
        public virtual T InstrumentId { get; set; }
        public virtual double Pip { get; set; }
        public InstrumentPairInterestRate InstrumentPairInterestRate { get; set; }

        public TradeableInstrument()
        {

        }

        public TradeableInstrument(string instrument):this(instrument,null)
        {
        }

        public TradeableInstrument(string instrument, string description)
            : this(instrument, default(T), description)
        {

        }
        public TradeableInstrument(string instrument, T instrumentId, string description)
            : this(instrument, instrumentId, 0, null)
        {
        }

        public TradeableInstrument(string instrument, double pip,
            InstrumentPairInterestRate instrumentPairInterestRate, String description)
            : this(instrument, default(T), pip, instrumentPairInterestRate, description)
        {

        }

        public TradeableInstrument( string instrument, T instrumentId,  double pip,
            InstrumentPairInterestRate instrumentPairInterestRate):this(instrument, instrumentId,pip, 
                instrumentPairInterestRate,null)
        {

        }

        public TradeableInstrument(string instrument, T instrumentId, double pip,
            InstrumentPairInterestRate instrumentPairInterestRate, string description)
        {
            Instrument = instrument;
            InstrumentId = instrumentId;
            Pip = pip;
            InstrumentPairInterestRate = instrumentPairInterestRate;
            Description = description;
        }

        public override int GetHashCode()
        {
            int prime = 31;
            int result = 1;
            result = prime * result + ((Instrument == null) ? 0 : Instrument.GetHashCode());
            result = prime * result + ((InstrumentId == null) ? 0 : InstrumentId.GetHashCode());
            return result;
        }

        public override bool Equals(object obj)
        {
            if(this == obj)
            {
                return true;
            }
            if(obj == null)
            {
                return false;
            }
            if(GetType() != obj.GetType())
            {
                return false;
            }
            TradeableInstrument<T> other = (TradeableInstrument<T>)obj;
            if(Instrument == null)
            {
                if(other.Instrument != null)
                {
                    return false;
                }
            }
            else if(!Instrument.Equals(other.Instrument))
            {
                return false;
            }
            if (InstrumentId == null)
            {
                if (other.InstrumentId != null)
                {
                    return false;
                }
            }
            else if (!InstrumentId.Equals(other.InstrumentId))
            {
                return false;
            }
            return true;
        }
    }

    
}
