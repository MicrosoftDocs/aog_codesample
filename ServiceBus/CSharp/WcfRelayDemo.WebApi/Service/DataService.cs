using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;

namespace Service
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "DataService" in both code and config file together.
    public class DataService : IDataService
    {       
        const string CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        const int LENGTH = 10;

        public string GetRandomString()
        {
            var random = new Random();
            return new string(Enumerable.Repeat(CHARS, LENGTH).Select(s => s[random.Next(s.Length)]).ToArray());
        }
    }
}
