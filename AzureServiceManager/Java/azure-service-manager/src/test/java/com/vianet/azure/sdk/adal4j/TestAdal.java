package com.vianet.azure.sdk.adal4j;

import com.vianet.azure.sdk.manage.AbstactTest;
import com.vianet.azure.sdk.manage.utils.AzureRestClient;

import javax.net.ssl.HttpsURLConnection;

import java.net.URL;

public class TestAdal extends AbstactTest {

    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String MANAGEMENT_ENDPOINT = "https://management.core.chinacloudapi.cn/";

    private final static String SUB_ID = "<your sub id>";
    private final static String USERNAME = "cietest03@microsoftinternal.partner.onmschina.cn";
    private final static String PASSWORD = "AzureCIE@M5";


    public static void main(String[] args) throws Exception {
        System.out.println(getCloudService(AzureRestClient.getAccessTokenFromUserCredentials("common", CLIENT_ID, USERNAME, PASSWORD, MANAGEMENT_ENDPOINT).getAccessToken()));
        System.exit(1);
    }

    public static String getCloudService(String accessToken) throws Exception {
        URL url = new URL(String.format("https://management.core.chinacloudapi.cn/%s/services/hostedservices", SUB_ID));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("x-ms-version", "2015-04-01");
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        String goodRespStr = AzureRestClient.getResponse(conn);
        return goodRespStr;
    }

}
