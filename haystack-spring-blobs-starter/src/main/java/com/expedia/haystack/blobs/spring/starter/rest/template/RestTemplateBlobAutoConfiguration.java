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

import com.expedia.haystack.blobs.spring.starter.BlobSettings;
import com.expedia.haystack.blobs.spring.starter.rest.template.interceptor.BlobAsyncRestTemplateInterceptor;
import com.expedia.haystack.blobs.spring.starter.rest.template.interceptor.BlobRestTemplateInterceptor;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingAsyncRestTemplateInterceptor;
import io.opentracing.contrib.spring.web.starter.RestTemplateTracingAutoConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.support.InterceptingAsyncHttpAccessor;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass({com.expedia.www.haystack.client.Tracer.class, RestTemplate.class})
@ConditionalOnProperty(value = "haystack.blobs.enabled", havingValue = "true")
@AutoConfigureAfter(RestTemplateTracingAutoConfiguration.class)
@EnableConfigurationProperties(BlobSettings.class)
public class RestTemplateBlobAutoConfiguration {
    private static final Log log = LogFactory.getLog(RestTemplateBlobAutoConfiguration.class);

    @Configuration
    @ConditionalOnBean(InterceptingHttpAccessor.class)
    public static class RestTemplatePostProcessingConfiguration {

        private final Set<InterceptingHttpAccessor> restTemplates;
        private final BlobRestTemplateCustomizer customizer;

        public RestTemplatePostProcessingConfiguration(final Set<InterceptingHttpAccessor> restTemplates) {
            this.restTemplates = restTemplates;
            this.customizer = new BlobRestTemplateCustomizer();
        }

        @PostConstruct
        public void init() {
            for (InterceptingHttpAccessor restTemplate : restTemplates) {
                registerBlobInterceptor(restTemplate);
            }
        }

        private void registerBlobInterceptor(InterceptingHttpAccessor restTemplate) {
            customizer.addInterceptor(restTemplate);
        }
    }

    /**
     * Injects {@link TracingAsyncRestTemplateInterceptor} into {@link InterceptingAsyncHttpAccessor#getInterceptors()}.
     * <p>
     * Note: From Spring Framework 5, {@link org.springframework.web.client.AsyncRestTemplate} is deprecated.
     */
    @Configuration
    @ConditionalOnBean(InterceptingAsyncHttpAccessor.class)
    @ConditionalOnClass(InterceptingAsyncHttpAccessor.class)
    public static class AsyncRestTemplatePostProcessingConfiguration {

        private final Set<InterceptingAsyncHttpAccessor> restTemplates;

        public AsyncRestTemplatePostProcessingConfiguration(final Set<InterceptingAsyncHttpAccessor> restTemplates) {
            this.restTemplates = restTemplates;
        }

        @PostConstruct
        public void init() {
            for (InterceptingAsyncHttpAccessor restTemplate : restTemplates) {
                registerBlobInterceptor(restTemplate);
            }
        }

        private void registerBlobInterceptor(InterceptingAsyncHttpAccessor restTemplate) {
            List<AsyncClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();

            for (AsyncClientHttpRequestInterceptor interceptor : interceptors) {
                if (interceptor instanceof BlobAsyncRestTemplateInterceptor) {
                    return;
                }
            }

            log.debug("Adding " + BlobAsyncRestTemplateInterceptor.class.getSimpleName() + " to " + restTemplate);
            interceptors = new ArrayList<>(interceptors);
            interceptors.add(new BlobAsyncRestTemplateInterceptor());
            restTemplate.setInterceptors(interceptors);
        }
    }

    /**
     * Provides {@link BlobRestTemplateCustomizer} bean, which adds {@link BlobRestTemplateInterceptor}
     * into default {@link RestTemplateBuilder} bean.
     * <p>
     * Supported only with Spring Boot.
     */
    @Configuration
    @ConditionalOnClass(RestTemplateCustomizer.class)
    public static class BlobRestTemplateCustomizerConfiguration {
        @Bean
        @ConditionalOnMissingBean(BlobRestTemplateCustomizer.class)
        public BlobRestTemplateCustomizer blobRestTemplateCustomizer() {
            return new BlobRestTemplateCustomizer();
        }
    }
}