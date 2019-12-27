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

import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFileClient;

/**
 * Created by chen.rui on 3/17/2016.
 */
public class StorageFileService {

    CloudFileClient client;

    public StorageFileService(CloudFileClient client) {
        this.client = client;
    }

    public CloudFileClient getClient() {
        return client;
    }


}
