/*
 * Copyright 2020 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

package com.expedia.haystack.blobs.spring.starter;

import com.expedia.blobs.core.BlobStore;
import com.expedia.blobs.stores.io.FileStore;
import com.expedia.haystack.agent.blobs.client.AgentClient;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties("haystack.blobs")
public class BlobSettings {
    private Store store = new Store();
    private boolean enabled = true;
    private double ratePerSec = -1;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setStore(Store store) {
        this.store = store;
    }
    public void setRatePerSec(double ratePerSec) {
        this.ratePerSec = ratePerSec;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public Store getStore() {
        return store;
    }
    public double getRatePerSec() {
        return ratePerSec;
    }

    public static class Store {
        private String name = "file";
        private String host = "haystack-agent";
        private int port = 35001;
        public void setName(String name) {
            this.name = name;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public void setPort(int port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }
        public String getHost() {
            return host;
        }
        public int getPort() {
            return port;
        }
    }

    public static BlobStore createBlobStore(final BlobSettings settings) {
        final BlobSettings.Store store = settings.getStore();
        switch (store.getName().toLowerCase()) {
            case "file": {
                String userDirectory = System.getProperty("user.dir");
                String directoryPath = userDirectory.concat("/blobs");
                File directory = new File(directoryPath);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                return new FileStore.Builder(directory).build();
            }
            case "agent": {
                return new AgentClient.Builder(store.getHost(), store.getPort()).build();
            }
            default:
                throw new UnsupportedOperationException("blob store type " + store.getName() + " is not supported");
        }
    }
}
