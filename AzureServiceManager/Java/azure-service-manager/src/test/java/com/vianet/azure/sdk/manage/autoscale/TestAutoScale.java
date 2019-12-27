package com.vianet.azure.sdk.manage.autoscale;

import com.vianet.azure.sdk.manage.utils.AzureRestClient;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestAutoScale {

    static String subscriptionId = "<your sub id>";
    static String cloudServiceName = "kevinpvm";
    static String availName = "kevinpm";

    @Test
    public void testAutoScale() throws Exception {
        String azureMgmtUri = String.format("https://management.core.chinacloudapi.cn/%s/services/monitoring/autoscalesettings?resourceId=/virtualmachines/%s/availabilitySets/%s",
                subscriptionId, cloudServiceName, availName);

        String body = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource("test.json").getFile().replace("%20"," ")));
        body = body.replace("$minInstances", "1");
        body = body.replace("$maxInstances", "2");
        body = body.replace("$defaultInstances", "1");
        body = body.replace("$cpuScaleOut", "80");
        body = body.replace("$cpuScaleIn", "50");
        System.out.println(body);
        byte[] data = body.getBytes();

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("x-ms-version", "2013-10-01");
        requestProperties.put("Content-Length", String.valueOf(data.length));
        requestProperties.put("Content-Type", "application/json;charset=utf-8");
        int code = AzureRestClient.processPutRequest(new URL(azureMgmtUri), data, requestProperties);
        assertEquals(code, 201);

    }

}
