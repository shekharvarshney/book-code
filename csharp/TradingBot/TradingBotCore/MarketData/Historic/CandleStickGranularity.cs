using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.MarketData.Historic
{
    public enum CandleStickGranularity:long
    {
        S5=5,
        S10=S5*2,
        S15=S5*3,
        S30=S5*6,
        M1=60,
        M2=M1*2,
        M3=M1*3,
        M5=M1*5,
        M10=M1*10,
        M15=M1*15,
        M30=M1*30,
        H1=3600,
        H2=H1*2,
        H3=H1*3,
        H4=H1*4,
        H6=H1*6,
        H8=H1*8,
        H12=H1*12,
        D=86400,
        W=604800,
        M=2592000
    }
}
