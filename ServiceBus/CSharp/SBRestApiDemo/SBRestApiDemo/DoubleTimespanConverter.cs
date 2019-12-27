using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SBRestApiDemo
{
    internal class DoubleTimespanConverter : JsonConverter
    {
        public override bool CanConvert(Type objectType)
        {
            return (objectType == typeof(TimeSpan));
        }

        public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
        {
            return TimeSpan.FromSeconds(Convert.ToDouble(reader.Value));
        }

        public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
        {
            if (value == null)
            {
                writer.WriteNull();
                return;
            }

            TimeSpan timeSpan = (TimeSpan)value;
            writer.WriteValue(timeSpan.TotalSeconds);
        }
    }
}
