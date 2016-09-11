using System.Collections.Generic;
using TradingBotCore.Account;
using TradingBotCore;
using OandaRESTApi.Utils;
namespace OandaRESTApi.Account
{
    public class OandaAccountDataProvider : IAccountProvider<long>
    {
        private readonly string baseUrl;
        private readonly string userName;
        private readonly string accessToken;

        public OandaAccountDataProvider(string baseUrl, string userName, string accessToken)
        {
            this.baseUrl = baseUrl;
            this.userName = userName;
            this.accessToken = accessToken;
        }

        public ICollection<Account<long>> LatestAccountInfo
        {
            get
            {
                string url = string.Format("{0}{1}?username={2}", 
                            this.baseUrl, OandaConstants.ACCOUNTS_RESOURCE, this.userName);
                OandaAccounts oandaAccounts = OandaUtils.OandaJsonToObject<OandaAccounts>(url, this.accessToken);
                IList<Account<long>> allAccounts = new List<Account<long>>();
                oandaAccounts.Accounts.ForEach(account => allAccounts.Add(getLatestAccountInfo(account.Id)));
                return allAccounts;
            }
        }

        public Account<long> getLatestAccountInfo(long accountId)
        {
            string url = string.Format("{0}{1}{2}{3}", this.baseUrl, OandaConstants.ACCOUNTS_RESOURCE, 
                TradingConstants.FWD_SLASH, accountId);
            OandaAccount account = OandaUtils.OandaJsonToObject<OandaAccount>(url, this.accessToken);
            return account;
        }
    }
}
