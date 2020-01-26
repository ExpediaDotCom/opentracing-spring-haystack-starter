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

package com.expedia.haystack.blobs.spring.starter.decorators;

import com.expedia.blobs.core.BlobContext;
import com.expedia.blobs.core.BlobType;
import com.expedia.blobs.core.BlobWriter;
import com.expedia.blobs.core.BlobsFactory;
import com.expedia.haystack.blobs.SpanBlobContext;
import com.expedia.haystack.blobs.spring.starter.Blobable;
import com.expedia.haystack.blobs.spring.starter.model.BlobContent;
import com.expedia.haystack.blobs.spring.starter.rest.template.BlobContainer;
import com.expedia.haystack.blobs.spring.starter.utils.BlobWriteHelper;
import io.opentracing.Span;
import io.opentracing.contrib.spring.web.client.RestTemplateSpanDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public class ClientRequestBlobDecorator implements RestTemplateSpanDecorator {
    private final static Logger log = LoggerFactory.getLogger(ClientRequestBlobDecorator.class);

    private final BlobsFactory<BlobContext> factory;
    private final Blobable blobable;

    public ClientRequestBlobDecorator(final BlobsFactory<BlobContext> factory,
                                      final Blobable blobable) {
        this.factory = factory;
        this.blobable = blobable;
    }

    @Override
    public void onRequest(final HttpRequest httpRequest, final Span span) {
        /* do nothing */
    }

    @Override
    public void onResponse(final HttpRequest httpRequest,
                           final ClientHttpResponse httpResponse,
                           final Span span) {
        doBlob(httpRequest, httpResponse, null, span);
    }

    @Override
    public void onError(final HttpRequest httpRequest,
                        final Throwable throwable,
                        final Span span) {
        doBlob(httpRequest, null, throwable, span);
    }

    private void doBlob(final HttpRequest httpRequest,
                        final ClientHttpResponse httpResponse,
                        final Throwable throwable,
                        final Span span) {
        if(blobable.isClientReqRespValidForBlob(httpRequest, httpResponse, throwable)
                && httpResponse instanceof BlobContainer) {

            final BlobContainer container = (BlobContainer)httpResponse;
            final SpanBlobContext blobContext = new SpanBlobContext((com.expedia.www.haystack.client.Span) span);
            /* what is better way for getting the content-type for request? default to application/octet-stream */
            write(container.getRequest(), blobContext, BlobType.REQUEST, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            write(container.getResponse(), blobContext, BlobType.RESPONSE, httpResponse.getHeaders().getContentType().getType());
        } else {
            log.debug("skip blob logging for client's request/response as condition has failed");
        }
    }

    private void write(byte[] data, SpanBlobContext blobContext, BlobType blobType, String contentType) {
        final BlobWriter writer = factory.create(blobContext);
        final BlobContent blob = new BlobContent(data, contentType, blobType);
        BlobWriteHelper.writeBlob(writer, blob);
    }
}
