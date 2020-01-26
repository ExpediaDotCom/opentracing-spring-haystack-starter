package com.expedia.haystack.blobs.spring.starter.rest.template;

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

        for (ClientHttpRequestInterceptor interceptor : interceptors) {
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
