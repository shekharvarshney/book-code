using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Net;
using System.IO;
namespace OandaRESTApi.Utils
{
    public static class OandaUtils
    {
        public static KeyValuePair<string,string> OandaAuthHeader(string accessToken)
        {
            return new KeyValuePair<string, string>("Authorization", "Bearer " + accessToken);
        }
        public static T OandaJsonToObject<T>(string url, string accessToken)
        {
            HttpWebRequest request = HttpWebRequest.Create(url) as HttpWebRequest;
            request.Method = RESTVerbs.GET.ToString();
            KeyValuePair<string, string> oandaAuthHdr = OandaAuthHeader(accessToken);
            request.Headers.Add(oandaAuthHdr.Key, oandaAuthHdr.Value);
            string oandaResponse = String.Empty;
            using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
            {
                Stream dataStream = response.GetResponseStream();
                StreamReader reader = new StreamReader(dataStream);
                oandaResponse = reader.ReadToEnd();
                reader.Close();
                dataStream.Close();
            }
            return JsonConvert.DeserializeObject<T>(oandaResponse);
        }
    }
}
