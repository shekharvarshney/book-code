using  TradingBotCore.Utils;
namespace TradingBotCore.Account
{
    public class Account<T>
    {
        public virtual double TotalBalance { get; set; }
        public virtual double UnRealisedPnl { get; set; }
        public virtual double RealisedPnl { get; set; }
        public virtual double MarginUsed { get; set; }
        public virtual double MarginAvailable { get; set; }
        public virtual double NetAssetValue
        {
            get
            {
                return MarginAvailable + TotalBalance;
            }
        }
        public virtual long OpenTrades { get; set; }
        public virtual string Currency { get; set; }
        public virtual T Id { get; set; }
        public virtual double AmountAvailableRatio
        {
            get
            {
                return MarginAvailable / TotalBalance;
            }
        }
        public virtual double MarginRate { get; set; }

        public override string ToString()
        {
            string toStr = string.Format("Currency={0},NAV={1},Total Balance={2}, UnrealisedPnl={3}, "
                + "RealisedPnl={4}, MarginUsed={5}, MarginAvailable={6},"
                + " OpenTrades={7}, amountAvailableRatio={8}, marginRate={9}", Currency, 
                NetAssetValue.ToString(TradingUtils.F2), TotalBalance.ToString(TradingUtils.F2),  
                UnRealisedPnl.ToString(TradingUtils.F2), RealisedPnl.ToString(TradingUtils.F2),
                MarginUsed.ToString(TradingUtils.F2), MarginAvailable.ToString(TradingUtils.F2),
                OpenTrades, AmountAvailableRatio.ToString(TradingUtils.F2),MarginRate.ToString(TradingUtils.F2)
                );
            return toStr;
        }

    }
}
