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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.vianet.azure.sdk.manage.Configure;
import org.junit.*;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

/**
 * Created by chen.rui on 3/18/2016.
 */
public class StorageFileTest {


    @Test
    public void testCreateFileContainer() throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, StorageException {
        Configure configure = new Configure();
        CloudFileClient client = AzureStorageServiceFactory.getInstance(configure).createFileService().getClient();

        CloudFileShare share = client.getShareReference("testshare");
        share.createIfNotExists();

        Iterable<CloudFileShare> shares = client.listShares();
        shares.forEach(new Consumer<CloudFileShare>() {
            @Override
            public void accept(CloudFileShare cloudFileShare) {
                System.out.println(cloudFileShare.getStorageUri());
            }
        });
    }

}
