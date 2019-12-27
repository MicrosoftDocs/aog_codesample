using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Globalization;

namespace SBRestApiDemo
{
    // referece https://docs.microsoft.com/en-us/dotnet/api/microsoft.servicebus.messaging.brokeredmessage#properties_summary
    class BrokerProperties
    {
        public string ContentType { get; set; }
        public string CorrelationId { get; set; }
        public string Label { get; set; }
        public bool ForcePersistence { get; set; }
        public string MessageId { get; set; }
        public string PartitionKey { get; set; }
        public string SessionId { get; set; }

        [JsonConverter(typeof(DoubleTimespanConverter))]
        public TimeSpan TimeToLive { get; set; }
        public string To { get; set; }
        public string ViaPartitionKey { get; set; }
        public string ReplyTo { get; set; }
        public string ReplyToSessionId { get; set; }
        public DateTime ScheduledEnqueueTimeUtc { get; set; }
        public string DeadLetterSource { get; set; }
        public int DeliveryCount { get; set; }
        public long EnqueuedSequenceNumber { get; set; }
        public DateTime EnqueuedTimeUtc { get; set; }
        public DateTime ExpiresAtUtc { get; set; }
        public bool IsBodyConsumed { get; set; }
        public DateTime LockedUntilUtc { get; set; }
        public Guid LockToken { get; set; }
        public long SequenceNumber { get; set; }
        public long Size { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public MessageState State { get; set; } = MessageState.Active;
    }

    enum MessageState
    {
        Deferred,
        Active,
        Scheduled
    }
}