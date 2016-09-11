using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OandaRESTApi
{
    internal enum RESTVerbs
    {
        GET, POST, PATCH, MODIFY
    }
    public static class OandaConstants
    {
        public const string ACCOUNTS_RESOURCE = "/v1/accounts";
        public const string INSTRUMENTS_RESOURCE = "/v1/instruments";
        public static readonly KeyValuePair<string,string> UNIX_DATETIME_HEADER 
                = new KeyValuePair<string, string>("X-Accept-Datetime-Format", "UNIX");
    }
}
