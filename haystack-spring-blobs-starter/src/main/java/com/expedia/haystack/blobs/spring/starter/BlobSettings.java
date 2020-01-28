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
    private String urlPattern = "/*";
    private int filterLevel = Integer.MIN_VALUE + 1;

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    public Store getStore() {
        return store;
    }

    public void setRatePerSec(double ratePerSec) {
        this.ratePerSec = ratePerSec;
    }
    public double getRatePerSec() {
        return ratePerSec;
    }

    public String getUrlPattern() {
        return urlPattern;
    }
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public void setFilterLevel(int filterLevel) {
        this.filterLevel = filterLevel;
    }

    public int getFilterLevel() {
        return filterLevel;
    }

    public static class Store {
        private String name = "file";
        private String directory;
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


        public String getDirectory() {
            return directory;
        }
        public void setDirectory(String directory) {
            this.directory = directory;
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
                final String dir =
                        (store.getDirectory() == null || store.getDirectory().isEmpty()) ?
                                System.getProperty("user.dir").concat("/blobs") : store.getDirectory();
                final File directory = new File(dir);
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
