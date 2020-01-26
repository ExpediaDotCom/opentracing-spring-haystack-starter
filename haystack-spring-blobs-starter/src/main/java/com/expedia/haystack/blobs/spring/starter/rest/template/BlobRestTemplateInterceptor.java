package com.expedia.haystack.blobs.spring.starter.rest.template;

import com.expedia.haystack.blobs.spring.starter.model.BlobWrappedClientHttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class BlobRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(final HttpRequest httpRequest,
                                        final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        final ClientHttpResponse httpResponse = execution.execute(httpRequest, body);
        return new BlobWrappedClientHttpResponse(body, httpResponse);
    }
}
