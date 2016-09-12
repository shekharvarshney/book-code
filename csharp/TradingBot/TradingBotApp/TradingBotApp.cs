using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Account;
using OandaRESTApi.Account;
using TradingBotCore.Instrument;
using OandaRESTApi.Instrument;
namespace TradingBotApp
{
    public class TradingBotApp
    {
        [STAThread]
        public static void Main(string[] args)
        {
            string url = "https://api-fxtrade.oanda.com";
            string token = "ea87e4266036daf85138f1d3b33aef48-ef5bcd7a7cc673e93ce4f9d62c5986de";
            string userName = "cvarshney";
            long accountId = 764454;
            //IAccountProvider<long> accProvider = new OandaAccountDataProvider(url, userName, token);
            //ICollection<Account<long>> ccc= accProvider.LatestAccountInfo;
            //Console.WriteLine("found accs=" + ccc.Count);

            IInstrumentDataProvider<string> instrProvider = new OandaInstrumentDataProvider(url, accountId, token);
            //ICollection<TradeableInstrument<string>> instrs =   instrProvider.Instruments;
            //Console.WriteLine("found instrs=" + instrs.Count);
            InstrumentService<string> instrService = new InstrumentService<string>(instrProvider);
            ICollection<TradeableInstrument<string>> audinstrs = instrService.getAllPairsWithCurrency("AUD");
            Console.WriteLine("found instrs=" + audinstrs.Count);
            Console.ReadLine();
        }
    }
}
