package com.expedia.haystack.blobs.spring.starter.rest.template;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;

public class BlobAsyncRestTemplateInterceptor implements AsyncClientHttpRequestInterceptor {
    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest httpRequest,
                                                          byte[] bytes,
                                                          AsyncClientHttpRequestExecution asyncClientHttpRequestExecution) throws IOException {
        return null;
    }
}
