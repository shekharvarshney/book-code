using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TradingBotCore.Account
{
    public interface IAccountProvider<T>
    {
        Account<T> getLatestAccountInfo(T accountId);
        ICollection<Account<T>> LatestAccountInfo
        {
            get;
        }
    }
}
