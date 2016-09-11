using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TradingBotCore.Account;
using OandaRESTApi.Account;
namespace TradingBotApp
{
    public class TradingBotApp
    {
        [STAThread]
        public static void Main(string[] args)
        {
            string url = "https://api-fxtrade.oanda.com";
            string token = "68d0378712450b16ce20ef0d8220eaf0-b2bf837a168d659007965e4538e7ae5d";
            string userName = "cvarshney";
            IAccountProvider<long> accProvider = new OandaAccountDataProvider(url, userName, token);

            ICollection<Account<long>> ccc= accProvider.LatestAccountInfo;
            Console.WriteLine("found accs=" + ccc.Count);
            Console.ReadLine();
        }
    }
}
