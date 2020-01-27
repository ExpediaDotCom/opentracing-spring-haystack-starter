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

package com.expedia.haystack.blobs.spring.starter.rest.template.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BlobAsyncRestTemplateInterceptor implements AsyncClientHttpRequestInterceptor {
    @Override
    public ListenableFuture<ClientHttpResponse> intercept(final HttpRequest httpRequest,
                                                          final byte[] body,
                                                          final AsyncClientHttpRequestExecution execution)
            throws IOException {

        final ListenableFuture<ClientHttpResponse> future = execution.executeAsync(httpRequest, body);
        return new ListenableFutureAdapter<ClientHttpResponse, ClientHttpResponse>(future) {
            @Override
            protected BlobWrappedClientHttpResponse adapt(final ClientHttpResponse httpResponse) throws ExecutionException {
                try {
                    return new BlobWrappedClientHttpResponse(body, httpResponse);
                } catch (IOException e) {
                    throw new ExecutionException(e);
                }
            }
        };
    }
}
