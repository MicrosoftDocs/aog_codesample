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

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.vianet.azure.sdk.manage.Configure;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.function.Consumer;


public class TestCacheControl {

    public static void main(String[] args) throws Exception {
        Configure configure = new Configure();
        final CloudBlobContainer container = AzureStorageServiceFactory.getInstance(configure).createBlobService().getContainerReference("cachecontroltest");

        System.out.println("ETag ==>" + container.getProperties().getEtag());
        System.out.println("LastModified ==>" + container.getProperties().getLastModified());
        System.out.println("LeaseDuration ==>" + container.getProperties().getLeaseDuration());
        System.out.println("LeaseState ==>" + container.getProperties().getLeaseState());
        System.out.println("LeaseStatus ==>" + container.getProperties().getLeaseStatus());

        container.getMetadata().keySet().forEach(new Consumer<String>() {
            @Override
            public void accept(String key) {
            System.out.println(key + "==>" + container.getMetadata().get(key));
            }
        });

        CloudBlockBlob blob = container.getBlockBlobReference("test.json");
        blob.downloadAttributes();
        System.out.println("CacheControl ==>" + blob.getProperties().getCacheControl());
    }


    private static InputStream getFileInputStream(String path) throws URISyntaxException {
        InputStream input = TestCacheControl.class.getClassLoader().getResourceAsStream(path);
        return input;
    }

}
