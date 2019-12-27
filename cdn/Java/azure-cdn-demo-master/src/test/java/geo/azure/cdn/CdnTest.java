package geo.azure.cdn;

import java.io.File;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class CdnTest {
	@Test
	public void test01() throws Exception {

		// 参考资料
		// ARM CDN REST: https://docs.microsoft.com/en-us/rest/api/cdn/endpoints 
		// ASM CDN REST: https://docs.azure.cn/zh-cn/cdn/cdn-api-create-endpoint 
		
		// ARM CDN REST:目前还没测试成功，后续研究下
/*		String requestURL = "https://restapi.cdn.azure.cn/subscriptions/e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb/resourceGroups/geogroup/providers/Microsoft.Cdn/profiles/geocdntest/endpoints/endponit01?api-version=2016-10-02";
		
		String requestTimestamp = TimeUtil.getUTCTime();
		String keyID = "cc65a046-2a32-4f7d-ab22-9ae49507d719";
		String keyValue = "N2IwNGE1OTgtMWJkOC00YjMwLTg3YjEtOWU1Yjg3MTYzOTA4";
		String httpMethod = "PUT";

		CdnOperation cOperation = new CdnOperation();
		String authorization = cOperation.calculateAuthorizationHeader(requestURL, requestTimestamp,
				keyID, keyValue, httpMethod);
		System.out.println(requestTimestamp);
		System.out.println(authorization);
		
		
		String requestBody = Files.toString(
				new File("D:\\workspace\\java\\azure-cdn-demo\\src\\test\\java\\geo\\azure\\cdn\\request_body.json"),
				Charsets.UTF_8);
		String result = cOperation.postRequest(requestURL,authorization, requestBody,requestTimestamp);
		System.out.println(result);*/
		
		// ASM CDN REST:添加节点，这里有个问题是request body中备案号涉及中文，门户上乱码显示
		String requestURL = "https://restapi.cdn.azure.cn/subscriptions/e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb/endpoints?apiVersion=1.0";
		String requestTimestamp = TimeUtil.getUTCTime();
		String keyID = "cc65a046-2a32-4f7d-ab22-9ae49507d719";
		String keyValue = "N2IwNGE1OTgtMWJkOC00YjMwLTg3YjEtOWU1Yjg3MTYzOTA4";
		String httpMethod = "POST";

		CdnOperation cOperation = new CdnOperation();
		String authorization = cOperation.calculateAuthorizationHeader(requestURL, requestTimestamp,
				keyID, keyValue, httpMethod);
		System.out.println(requestTimestamp);
		System.out.println(authorization);
		
		
		String requestBody = Files.toString(
				new File("D:\\workspace\\java\\azure-cdn-demo\\src\\test\\java\\geo\\azure\\cdn\\request_body.json"),
				Charsets.UTF_8);
		String result = cOperation.postRequest(requestURL,authorization, requestBody,requestTimestamp);
		System.out.println(result);
		
		// ASM CDN REST:获取节点
	/*	requestURL = "https://restapi.cdn.azure.cn/subscriptions/e0fbea86-6cf2-4b2d-81e2-9c59f4f96bcb/endpoints/8137ecc4-71c4-11e7-8259-0017fa00a611?apiVersion=1.0";
		requestTimestamp = TimeUtil.getUTCTime();
		httpMethod = "GET";
		authorization = cOperation.calculateAuthorizationHeader(requestURL, requestTimestamp,
				keyID, keyValue, httpMethod);
		System.out.println(requestTimestamp);
		System.out.println(authorization);
		result = cOperation.getRequest(requestURL, authorization, requestTimestamp);
		System.out.println(result);*/
	}
}
