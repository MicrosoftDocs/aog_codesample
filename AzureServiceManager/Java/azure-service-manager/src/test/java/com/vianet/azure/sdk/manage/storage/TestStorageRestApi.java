/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.storage;

import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.Utility;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static java.util.Base64.getEncoder;


public class TestStorageRestApi {

    public static void main(String[] args) throws Exception  {
        String storageAccount = "devstorage";
        String storageKey = "GVbNd6Ntz4xfXyPAQ4GspP2oIePrshR5L3ws8oXZJQKr6RWmGGThxnAldwAp7Eh0sQlVAtaZDVV0BygaS2u+Yg==";
        TestStorageRestApi restApi = new TestStorageRestApi();
        String authHeader = restApi.getAuthHeader(storageAccount, storageKey);
        System.out.println(authHeader);
    }

    public String getAuthHeader(String storageAccount, String storageKey) throws Exception {
        String date = Utility.getGMTTime();
//        String stringToSign = "PUT\n" +
//                "\n" +
//                "\n" +
//                "4\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "x-ms-blob-content-md5:CY9rzUYh03PK3k6DJie09g==\n" +
//                "x-ms-blob-type:BlockBlob\n" +
//                "x-ms-client-request-id:d54c10f0-6722-4a0a-b4a9-965ad3435031\n" +
//                "x-ms-date:Thu, 16 Mar 2017 15:05:26 GMT\n" +
//                "x-ms-version:2015-04-05\n" +
//                "/devstorage/test/ts1";
        String  stringToSign = "PUT\n" +
                "\n" +
                "text/plain; charset=UTF-8\n" +
                "\n" +
                "x-ms-blob-content-disposition:attachment; filename=\"chrome_100_percent.pak\"\n" +
                "x-ms-blob-type:BlockBlob\n" +
                "x-ms-date:Fri, 17 Mar 2017 08:53:38 GMT\n" +
                "x-ms-version:2016-05-31\n" +
                "/devstorage/test/myblob";

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        System.out.println(stringToSign);
        byte[] decodes = Base64.decode(storageKey);
        hmacSha256.init(new SecretKeySpec(decodes, "HmacSHA256"));

        String signature = getEncoder().encodeToString(hmacSha256.doFinal(stringToSign.getBytes("UTF-8")));
        String signatureHeader = "SharedKey " + storageAccount + ":"+signature;
        return signatureHeader;
    }

}
