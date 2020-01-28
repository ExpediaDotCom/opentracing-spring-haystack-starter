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

package com.expedia.haystack.blobs.spring.starter.rest.template;

import com.expedia.haystack.blobs.spring.starter.rest.template.interceptor.BlobRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class BlobRestTemplateCustomizer implements RestTemplateCustomizer {
    private final static Logger log = LoggerFactory.getLogger(BlobRestTemplateCustomizer.class);

    @Override
    public void customize(RestTemplate restTemplate) {
        addInterceptor(restTemplate);
    }

    void addInterceptor(InterceptingHttpAccessor restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();

        for (final ClientHttpRequestInterceptor interceptor : interceptors) {
            if (interceptor instanceof BlobRestTemplateInterceptor) {
                return;
            }
        }

        log.debug("Adding " + BlobRestTemplateInterceptor.class.getSimpleName() + " to " + restTemplate);
        interceptors = new ArrayList<>(interceptors);
        interceptors.add(new BlobRestTemplateInterceptor());
        restTemplate.setInterceptors(interceptors);
    }
}
