package com.vianet.azure.sdk.manage.billing;

import com.vianet.azure.sdk.manage.AbstactTest;
import com.vianet.azure.sdk.manage.utils.AzureRestClient;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URL;


public class TestResourceUsage {

    public final static String MANAGEMENT_EBDPOINT = "https://management.chinacloudapi.cn/";

    @Test
    public void rateCardTest() throws Exception {
        String token = AzureRestClient.getAccessTokenFromClientCredentials(AbstactTest.TENTANT, AbstactTest.CLIENT_ID, AbstactTest.CLIENT_SECRET, MANAGEMENT_EBDPOINT);
        String filter = String.format("api-version=%s&$filter=OfferDurableId eq '%s' and Currency eq '%s' and Locale eq '%s' and RegionInfo eq '%s'",
                "2016-08-31-preview",
                "MS-MC-AZR-0033P",
                "CNY",
                "en-US",
                "CN");
        URI uri = new URI("https", "management.chinacloudapi.cn",
                String.format("/subscriptions/%s/providers/Microsoft.Commerce/RateCard", AbstactTest.SUB_ID), filter, null);
        URL url = uri.toURL();
        System.out.println(url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + token);

        String result = AzureRestClient.getResponse(conn);
        System.out.println(result);
    }

    @Test
    public void UsageAggregatesTest() throws Exception {
        String token = AzureRestClient.getAccessTokenFromClientCredentials(AbstactTest.TENTANT, AbstactTest.CLIENT_ID, AbstactTest.CLIENT_SECRET, MANAGEMENT_EBDPOINT);
        String filter = String.format("api-version=%s&reportedStartTime=%s&reportedEndTime=%s&aggregationGranularity=%s&showDetails=%s",
                "2015-06-01-preview",
                "2017-04-01 00:00:00",
                "2017-04-30 00:00:00",
                "Daily",
                "true");
        URI uri = new URI("https", "management.chinacloudapi.cn",
                String.format("/subscriptions/%s/providers/Microsoft.Commerce/UsageAggregates", AbstactTest.SUB_ID), filter, null);
        URL url = uri.toURL();
        System.out.println(url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + token);

        String result = AzureRestClient.getResponse(conn);
        System.out.println(result);
    }

}
