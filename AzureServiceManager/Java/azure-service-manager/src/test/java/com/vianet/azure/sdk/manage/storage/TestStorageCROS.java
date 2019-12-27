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

import com.microsoft.azure.storage.CorsHttpMethods;
import com.microsoft.azure.storage.CorsProperties;
import com.microsoft.azure.storage.CorsRule;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.vianet.azure.sdk.manage.Configure;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by chen.rui on 3/10/2016.
 */
public class TestStorageCROS {

    public static void main(String[] args) throws Exception {
        Configure configure = new Configure();
        CloudBlobClient client = AzureStorageServiceFactory.getInstance(configure).createBlobService().getClient();
        ServiceProperties serviceProperties = new ServiceProperties();
        CorsProperties cors = new CorsProperties();

        CorsRule corsRule = new CorsRule();
        corsRule.setAllowedHeaders(Arrays.asList("x-ms-blob-content-type","x-ms-blob-content-disposition"));
        corsRule.setAllowedMethods(EnumSet.of(CorsHttpMethods.HEAD, CorsHttpMethods.GET, CorsHttpMethods.POST, CorsHttpMethods.PUT));
        corsRule.setAllowedOrigins(Arrays.asList("*"));
        corsRule.setExposedHeaders(Arrays.asList("x-ms-*"));
        corsRule.setMaxAgeInSeconds(3600);
        cors.getCorsRules().add(corsRule);

        serviceProperties.setCors(cors);
        client.uploadServiceProperties(serviceProperties);
    }


    private static InputStream getFileInputStream(String path) throws URISyntaxException {
        InputStream input = TestStorageCROS.class.getClassLoader().getResourceAsStream(path);
        return input;
    }

}
