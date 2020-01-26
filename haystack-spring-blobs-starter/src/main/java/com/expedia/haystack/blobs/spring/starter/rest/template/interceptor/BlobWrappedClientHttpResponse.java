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

import com.expedia.haystack.blobs.spring.starter.rest.template.BlobContainer;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlobWrappedClientHttpResponse implements ClientHttpResponse, BlobContainer {

    private final ClientHttpResponse httpResponse;
    private final ByteArrayInputStream wrappedStream;
    private final byte[] responseBytes;
    private final byte[] requestBytes;

    public BlobWrappedClientHttpResponse(byte[] requestBytes, ClientHttpResponse httpResponse) throws IOException {
        this.httpResponse = httpResponse;
        this.requestBytes = requestBytes;

        try(final InputStream in = httpResponse.getBody()) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(httpResponse.getBody(), baos);
            responseBytes = baos.toByteArray();
            wrappedStream = new ByteArrayInputStream(responseBytes);
        }
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return httpResponse.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return httpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return httpResponse.getStatusText();
    }

    @Override
    public void close() { }

    @Override
    public InputStream getBody() throws IOException {
        return wrappedStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpResponse.getHeaders();
    }

    public byte[] getResponse() {
        return responseBytes;
    }

    public byte[] getRequest() {
        return requestBytes;
    }
}
