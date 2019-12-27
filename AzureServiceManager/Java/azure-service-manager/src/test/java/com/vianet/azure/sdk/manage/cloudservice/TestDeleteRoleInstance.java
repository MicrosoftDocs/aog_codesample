package com.vianet.azure.sdk.manage.cloudservice;

import com.vianet.azure.sdk.manage.utils.AzureRestClient;
import org.junit.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestDeleteRoleInstance {

    static String subscriptionId = "<your sub id>";
    static String cloudServiceName = "maywadexample";
    static String deploymentslot = "staging";
    static String roleInstaceName = "WorkerRole1_IN_1";

    @Test
    public void testDeleteRoleInstance() throws Exception  {
        String endpoint = String.format("https://management.core.chinacloudapi.cn/%s/services/hostedservices/%s/deploymentslots/%s/roleinstances/?comp=delete",
                subscriptionId, cloudServiceName, deploymentslot);

        String bodyFormat = "<RoleInstances xmlns=\"http://schemas.microsoft.com/windowsazure\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">%s</RoleInstances>";
        String roleInstance = String.format("<Name>%s</Name>", roleInstaceName);
        byte[] data = String.format(bodyFormat, roleInstance).getBytes();

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("x-ms-version", "2013-08-01");
        requestProperties.put("Content-Length", String.valueOf(data.length));
        requestProperties.put("Content-Type", "application/xml");
        int code = AzureRestClient.processPostRequest(new URL(endpoint), data, requestProperties);
        assertEquals(code, 202);
    }
    
    @Test
    public void testGetCloudService() throws Exception  {
		String url = String.format("https://management.core.chinacloudapi.cn/%s/services/hostedservices", subscriptionId);
		Map<String, String> params = new HashMap<String, String>();
		params.put("x-ms-version", "2013-08-01");
		params.put("Content-Type", "application/xml");
		String responseTxt = AzureRestClient.processGetRequest(new URL(url), params);
		System.out.println(responseTxt);
    }

}
