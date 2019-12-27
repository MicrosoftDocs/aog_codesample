package com.vianet.azure.sdk.manage.monitor;

import com.vianet.azure.sdk.manage.utils.AzureRestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.*;
import java.util.Base64;

public class EventHubMetricApi {


    public static void main(String[] args) throws Exception {
        EventHubMetricApi metricApi = new EventHubMetricApi();
        String res =  metricApi.sendRequestUseCert("e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb",  "vikev-ns","vikev", "E:\\Documents\\CIETest03.publishsettings");
        System.out.println(res);
    }


    public String sendRequestUseCert(String subId, String namespace, String eventhub, String publishSettingFile) throws Exception {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/ServiceBus/namespaces/%s/eventhubs/%s/Metrics", subId, namespace, eventhub));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("x-ms-version", "2012-03-01");
        conn.setSSLSocketFactory(AzureRestClient.getSSLSocketFactory(subId, publishSettingFile));
        String response = AzureRestClient.getResponse(conn);
        return response;
    }


    public String sendRequest(String subId, String namespace, String eventhub) throws IOException {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/ServiceBus/namespaces/%s/eventhubs/%s/Metrics", subId, namespace, eventhub));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("x-ms-version", "2012-03-01");
        conn.setRequestProperty("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6InRSc2pFYk9QY1VfRmxJejJwSFMyVVZCSXNobyIsImtpZCI6InRSc2pFYk9QY1VfRmxJejJwSFMyVVZCSXNobyJ9.eyJhdWQiOiJodHRwczovL21hbmFnZW1lbnQuY29yZS5jaGluYWNsb3VkYXBpLmNuLyIsImlzcyI6Imh0dHBzOi8vc3RzLmNoaW5hY2xvdWRhcGkuY24vYjM4OGI4MDgtMGVjOS00YTA5LWE0MTQtYTdjYmJkOGI3ZTliLyIsImlhdCI6MTQ5MDM0NzA3NiwibmJmIjoxNDkwMzQ3MDc2LCJleHAiOjE0OTAzNTA5NzYsImFjciI6IjEiLCJhaW8iOiJBUUFCQUFFQUFBQ3JIS3ZyeDdHMlNhWmJaaC10RG5wN1hrVWVrTXFadW1kenlIdl9FS3BtUlZCYjlqODZjbklOQXQ3dXpmSjhsSTZ1ZUxkbDVoc3dmdGhPSWZ1X2VQZGdFd1lOUk1GT0l6YXJHYWwyYnIzajJ4elZrOC1XejBoOTNmeVZ5akl4dlljZ0FBIiwiYW1yIjpbInB3ZCJdLCJhcHBpZCI6ImM0NGI0MDgzLTNiYjAtNDljMS1iNDdkLTk3NGU1M2NiZGYzYyIsImFwcGlkYWNyIjoiMiIsImVfZXhwIjoxMDc5OSwiZmFtaWx5X25hbWUiOiJUZXN0MDMiLCJnaXZlbl9uYW1lIjoiQ0lFIiwiaXBhZGRyIjoiMTA2LjEyMC43OC4xOTAiLCJuYW1lIjoiQ0lFIFRlc3QwMyIsIm9pZCI6Ijc2ODY5NmJiLWFiNWUtNDRjNi04ZTFiLTcxMjJiNjFiNWU4MSIsInBsYXRmIjoiMyIsInB1aWQiOiIyMDAzM0ZGRjgwMDFCOUQwIiwic2NwIjoidXNlcl9pbXBlcnNvbmF0aW9uIiwic3ViIjoiZllWRGNEdEpPOWpxNjRVSWVTS2h2dzctWHdTUmo4bDlyMm9nelJFWTBSWSIsInRpZCI6ImIzODhiODA4LTBlYzktNGEwOS1hNDE0LWE3Y2JiZDhiN2U5YiIsInVuaXF1ZV9uYW1lIjoiQ0lFVGVzdDAzQE1pY3Jvc29mdEludGVybmFsLnBhcnRuZXIub25tc2NoaW5hLmNuIiwidXBuIjoiQ0lFVGVzdDAzQE1pY3Jvc29mdEludGVybmFsLnBhcnRuZXIub25tc2NoaW5hLmNuIiwidmVyIjoiMS4wIn0.Kbm7YoxA4AdVQGgsg4ZZ4ILsFK3aTt4EBIR0scsTWJEgcVj9kXcGDFZ0lJq4FH4aiFRmLUXJJ-qKdqMIWoeiZyDlYlMHtzfFBNsoESGv9mhvniykXCLcknLN5xzBY7l0LfIaZ0FLqaF90T9sDq6khZPPtNak5Aom76TATZsDyYKBS17WYVI3YBqviakzzhnbVK1mgY5ixuEXdwNMLteL9gFDkjiXiAlaZbFakd8FxjYThAnoc7nFAN5iwau-TZQFTALiZJ99c9lGywySA7XYGI7EBOLTIodsFc1jIwaySob7HcUQS3miGwFTvIkJt5757tEUNuS7sdc3T--J1bz2DA");
//        conn.setRequestProperty("Authorization", getSASToken(keyName, key, "https://management.core.chinacloudapi.cn"));
        String response = AzureRestClient.getResponse(conn);
        return response;
    }


    private static String getSASToken(String resourceUri, String keyName, String key)
    {
        long epoch = System.currentTimeMillis()/1000L;
        int week = 60*60*24*7;
        String expiry = Long.toString(epoch + week);

        String sasToken = null;
        try {
            String stringToSign = URLEncoder.encode(resourceUri, "UTF-8") + "\n" + expiry;
            String signature = getHMAC256(key, stringToSign);
            sasToken = "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri, "UTF-8") +"&sig=" +
                    URLEncoder.encode(signature, "UTF-8") + "&se=" + expiry + "&skn=" + keyName;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(sasToken);
        return sasToken;
    }

    public static String getHMAC256(String key, String input) {
        Mac sha256_HMAC = null;
        String hash = null;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            Base64.Encoder encoder = Base64.getEncoder();

            hash = new String(encoder.encode(sha256_HMAC.doFinal(input.getBytes("UTF-8"))));

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return hash;
    }

}
