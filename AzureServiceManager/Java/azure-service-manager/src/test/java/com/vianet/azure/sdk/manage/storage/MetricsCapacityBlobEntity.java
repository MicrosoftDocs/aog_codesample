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

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableEntity;
import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by chen.rui on 3/25/2016.
 */
public class MetricsCapacityBlobEntity  extends TableServiceEntity {

    Long capacity;
    Long containerCount;
    Long objectCount;


    public MetricsCapacityBlobEntity(Long capacity, Long containerCount, Long objectCount) {
        this.capacity = capacity;
        this.containerCount = containerCount;
        this.objectCount = objectCount;
    }

    public MetricsCapacityBlobEntity() {}

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public Long getContainerCount() {
        return containerCount;
    }

    public void setContainerCount(Long containerCount) {
        this.containerCount = containerCount;
    }

    public Long getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(Long objectCount) {
        this.objectCount = objectCount;
    }
}