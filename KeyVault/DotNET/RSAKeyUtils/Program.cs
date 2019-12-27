using System;

namespace PKCS1ToPKCS8Application
{
    class Program
    {
        static void Main(string[] args)
        {
            byte[] importedPrivateKeyBytes = Convert.FromBase64String("MIIEowIBAAKCAQEAtuHuMvnE3fYz/eom8mF7xxLXPETjfUpVYHL6AUVwXKKCrZ6uFszh2bY7ADoiIpuPTS0NVNYeKLlu6T59qzyr0bnT8KgXdZJFVzYWIh8VOLTb2zghlu8BeBoJP9t/a34Mj7gGJUZWBgECopEGCXrFwyIvXSW0JU5YrJinyyPhUjUXLeyowJUXoLwEafOHBRRiZZv1SNlntQOAenHFB/1uv2Y5xmdHm7xl+jjeo469u+pY535RCekmam9hWbZcn/WZEf9BZEmE2i4v7Qj72V92LrSUSSjF6w6lgU3B+Byu0XR8ZeUjD+FP9ZKvJyEtxlxOVaADQ+/udghUr1PnfXIHRQIDAQABAoIBAHG1UBFJ0unfJrx9VfHmQruoL0M94eQIz8TEOEWKEy7FrFKfEscCZHqlH1Io0wiJiDQICv3wk5fmk9taC3DorDweOnSrTsq/Q3XSHzjf8qXrbbeD0v6xZEx0g8O8iiEfolfJp6iNbvcUsbq6SPKj70pAewqDYtq/N8s4rztS98nQQ5PAQ10Zv3X910lc0vnPlPKew4y7w6hwb/pbiEBLbbV9tYAVaAm63n/EYxjo3UCRA0BJZxhJbRdEQzCEzClXlo6txx7nGQz/omv574P22ASCAfyrFNhaT20DR+ilIXe+edBpJehD6Q/lpO56STawb9xCAk6+aerM5dcIBMTETRECgYEA3RdnfpY1Z8KtOT1rVUeI094vylP1HONrcXNSsV0sbNPSwoK91UpO0mfsLLWxLtjvX52yixhfKxQ0QLYq3nyC8SRj2GE07aO1HZ81MI/9Y6ynJkw+J0nsSc8cBpu1hEUwjTgQu0GnKZtbjaZKZ7dMbtzycpUUrVjf/+34Ds2RC4sCgYEA08Ib+c6EMouffAtcEFN0fsI0HTavxJkGjZC2KuN2LMLeddiDoAKNNrl1kEwHL2ImP7g9ED4U97BJ43ekiwq4sbCDhnw+Ydxtdw2K2D3osbYIqhUPTM86YkjOkBZiU/TutVyacOTOwrzvNBLZfVn24Kxb2ehGLbsh8ar7OPsC0m8CgYBvEETDSH3Hg/o02O/ERU0s8V6cixSE0JG2yjHuO1oHyVkEsVzfepaiB+aShytc10lYhQWd7j5Qi7O8FkbuwSmeLaOinSJThnuDR+kWXh7yJVdKW96VKgNzCIGRqELFEWTUbCeric2Jjyusuq6B74iL4J5ChTV/5zePf2GvPgDxBQKBgEbgwdxrc3Q3p7otUzNju6px9l+Q3mQ/lCHuPgs892EkGLH5NpVoRTv7943E5OLHR2YslMPLA9mj+BdbBIhFnmxbWKq7C2ZWEY23yrF2h1x0QZIsWmKrOi+LjxhLZb4UTZIG3OrMqygjdS1I4eNDKY8qIdReGp9T85igUlZQLjGhAoGBAI3/3blQyHMUd1t9FbfQNDlkTAYWqUX+9Wgt34AGXjVg68Z96SzAw7rO0flnskLr6TlA/Ns21cfV1XYosK3oExbkaxqTyLHdAufKH0LPSZyaUQdQWKa9U3qMDJy78WJbaA7RmGuVfdB3mE60/iksXs0kC6R9E27gUPjopHp7Ccz2");
            var privKeyPKCS8 = RSAKeyUtils.PrivateKeyToPKCS8(importedPrivateKeyBytes);
            string pkcs8privKeyBase64 = Convert.ToBase64String(privKeyPKCS8);

            Console.WriteLine("------   PRIVATE KEY TO EXPORT   ------");
            Console.WriteLine(pkcs8privKeyBase64);
            Console.ReadKey();
         }
    }
}