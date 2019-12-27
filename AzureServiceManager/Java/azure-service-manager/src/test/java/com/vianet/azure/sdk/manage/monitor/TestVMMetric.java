package com.vianet.azure.sdk.manage.monitor;


import com.vianet.azure.sdk.manage.AbstactTest;
import com.vianet.azure.sdk.manage.utils.AzureRestClient;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TestVMMetric {

    @Test
    public void GetMetricDefineTest() throws Exception {
        String token = AzureRestClient.getAccessTokenFromClientCredentials(AbstactTest.TENTANT, AbstactTest.CLIENT_ID, AbstactTest.CLIENT_SECRET, AbstactTest.MANAGEMENT_EBDPOINT);
        String filter =  String.format("api-version=%s", "2015-07-01");
        String resourceId = String.format("/subscriptions/%s/resourceGroups/kevin-vs-never/providers/Microsoft.ClassicCompute/virtualMachines/kevin-vs-never", AbstactTest.SUB_ID);

        URI uri = new URI("https", "management.chinacloudapi.cn",
                String.format("%s/providers/microsoft.insights/metricdefinitions", resourceId), filter, null);
        URL url = uri.toURL();
        System.out.println(url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + token);

        String result = AzureRestClient.getResponse(conn);
        System.out.println(result);
    }

    @Test
    public void GetUsageTest() throws Exception {
        String token = AzureRestClient.getAccessTokenFromClientCredentials(AbstactTest.TENTANT, AbstactTest.CLIENT_ID, AbstactTest.CLIENT_SECRET, AbstactTest.MANAGEMENT_EBDPOINT);
        String filter =  String.format("api-version=%s",
                "2015-05-01-preview");
        String resourceId = String.format("/subscriptions/%s/providers/Microsoft.Compute/locations/chinanorth/usages", AbstactTest.SUB_ID);

        URI uri = new URI("https", "management.chinacloudapi.cn",
                String.format("%s/usages", resourceId), filter, null);
        URL url = uri.toURL();
        System.out.println(url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + token);

        String result = AzureRestClient.getResponse(conn);
        System.out.println(result);
    }

    @Test
    public void GetMetricDataTest() throws Exception {
        String token = AzureRestClient.getAccessTokenFromClientCredentials(AbstactTest.TENTANT, AbstactTest.CLIENT_ID, AbstactTest.CLIENT_SECRET, AbstactTest.MANAGEMENT_EBDPOINT);
        String filter =  String.format("api-version=%s&$filter=startTime eq 2017-04-5 and endTime eq 2017-04-6 and timeGrain eq duration'PT1H'",
                "2016-06-01");

        String resourceId = String.format("/subscriptions/%s/resourceGroups/kevin-vs-never/providers/Microsoft.ClassicCompute/virtualMachines/kevin-vs-never", AbstactTest.SUB_ID);

        URI uri = new URI("https", "management.chinacloudapi.cn",
                String.format("%s/providers/microsoft.insights/metrics", resourceId), filter, null);
        URL url = uri.toURL();
        System.out.println(url);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization","Bearer " + token);

        String result = AzureRestClient.getResponse(conn);
        System.out.println(result);
    }

    @Test
    public void GetMetricDefineOldTest() throws Exception {
        String filter =  String.format("resourceId=/hostedservices/%s/deployments/%s/roles/%s",
                "kevin-vs-never",
                "kevin-vs-never",
                "kevin-vs-never");
        URI uri = new URI("https", "management.core.chinacloudapi.cn",
                String.format("/%s/services/monitoring/metricdefinitions/query", AbstactTest.SUB_ID), filter, null);
        URL url = uri.toURL();
        Map<String, String> params = new HashMap<String, String>();
        params.put("x-ms-version", "2013-10-01");
        params.put("Content-Type", "application/xml");
        String result = AzureRestClient.processGetRequest(url, params);
        System.out.println(result);
    }

    @Test
    public void GetMetricOldTest() throws Exception {
        String filter =  String.format("resourceId=/hostedservices/%s/deployments/%s/roles/%s&namespace=%s&names=%s&timeGrain=%s&startTime=%s&endTime=%s",
                "kevin-vs-never",
                "kevin-vs-never",
                "kevin-vs-never",
                "",
                "Network In,Network Out,Disk Read Bytes/sec,Disk Write Bytes/sec",
                "PT1H",
                "2017-04-03T00:00:00.0000000Z",
                "2017-04-05T23:59:59.0000000Z");
        URI uri = new URI("https", "management.core.chinacloudapi.cn",
                String.format("/%s/services/monitoring/metricvalues/query", AbstactTest.SUB_ID), filter, null);
        URL url = uri.toURL();
        Map<String, String> params = new HashMap<String, String>();
        params.put("x-ms-version", "2013-10-01");
        params.put("Content-Type", "application/xml");
        String result = AzureRestClient.processGetRequest(url, params);
        System.out.println(result);
    }

}
