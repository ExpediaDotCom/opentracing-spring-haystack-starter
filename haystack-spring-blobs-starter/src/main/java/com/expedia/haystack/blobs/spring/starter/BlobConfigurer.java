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

import com.expedia.blobs.core.BlobContext;
import com.expedia.blobs.core.BlobStore;
import com.expedia.blobs.core.BlobsFactory;
import com.expedia.blobs.core.predicates.BlobsRateLimiter;
import com.expedia.haystack.blobs.spring.starter.decorators.ClientRequestBlobDecorator;
import com.expedia.haystack.blobs.spring.starter.decorators.ServerRequestBlobDecorator;
import com.expedia.haystack.blobs.spring.starter.filter.BlobFilter;
import io.opentracing.contrib.spring.web.client.RestTemplateSpanDecorator;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Predicate;

@Configuration
@ConditionalOnClass(com.expedia.www.haystack.client.Tracer.class)
@ConditionalOnProperty(value = "haystack.blobs.enabled", havingValue = "true")
@EnableConfigurationProperties(BlobSettings.class)
public class BlobConfigurer {

    @Bean
    @ConditionalOnClass(RestTemplateSpanDecorator.class)
    public ClientRequestBlobDecorator clientBlobDecorator(BlobsFactory<BlobContext> factory, Blobable blobable) {
        return new ClientRequestBlobDecorator(factory, blobable);
    }

    @Bean
    public ServletFilterSpanDecorator serverBlobDecorator(BlobsFactory<BlobContext> factory, Blobable blobable) {
        return new ServerRequestBlobDecorator(factory, blobable);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobsFactory<BlobContext> blobFactory(BlobSettings settings, BlobStore blobStore) {
        if (!settings.isEnabled()) return null;
        final Predicate<BlobContext> predicate = settings.getRatePerSec() >= 0 ?
                new BlobsRateLimiter<>(settings.getRatePerSec()) : t -> true;
        return new BlobsFactory<>(blobStore, predicate);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobStore blobStore(BlobSettings settings) {
        return BlobSettings.createBlobStore(settings);
    }

    @Bean
    @ConditionalOnMissingBean
    public Blobable blobable() {
        return Blobable.DEFAULT_BLOBABLE;
    }

    @Bean
    public FilterRegistrationBean blobFilter(BlobSettings settings) {
        final BlobFilter blobFilter = new BlobFilter();
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(blobFilter);
        filterRegistrationBean.addUrlPatterns(settings.getUrlPattern());
        filterRegistrationBean.setOrder(settings.getFilterLevel());
        filterRegistrationBean.setAsyncSupported(true);
        return filterRegistrationBean;
    }
}
