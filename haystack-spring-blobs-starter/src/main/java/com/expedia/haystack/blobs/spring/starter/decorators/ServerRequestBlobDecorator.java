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
import com.expedia.haystack.blobs.spring.starter.filter.BlobFilter;
import com.expedia.haystack.blobs.spring.starter.model.BlobContent;
import com.expedia.haystack.blobs.spring.starter.utils.BlobWriteHelper;
import io.opentracing.Span;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeoutException;

public class ServerRequestBlobDecorator implements ServletFilterSpanDecorator {
    private final static Logger log = LoggerFactory.getLogger(ServerRequestBlobDecorator.class);
    private final BlobsFactory<BlobContext> factory;
    private final Blobable blobable;

    public ServerRequestBlobDecorator(BlobsFactory<BlobContext> factory, Blobable blobable) {
        this.factory = factory;
        this.blobable = blobable;
    }

    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {
        /* do nothing*/
    }

    @Override
    public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Span span) {
        doBlob(httpServletRequest, httpServletResponse, null, span);
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable throwable, Span span) {
        doBlob(httpServletRequest, httpServletResponse, throwable, span);
    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long timeout, Span span) {
        doBlob(httpServletRequest, httpServletResponse, new TimeoutException(), span);
    }

    private void doBlob(HttpServletRequest servletReq, HttpServletResponse servletResponse, Throwable throwable, Span span) {
        if (blobable.isServerReqRespValidForBlob(servletReq, servletResponse, throwable)) {
            final SpanBlobContext blobContext = new SpanBlobContext((com.expedia.www.haystack.client.Span) span);

            final Object requestBytes = servletReq.getAttribute(BlobFilter.REQUEST_BLOB_KEY);
            write(requestBytes, blobContext, BlobType.REQUEST, servletReq.getContentType());

            final Object responseBytes = servletReq.getAttribute(BlobFilter.RESPONSE_BLOB_KEY);
            write(responseBytes, blobContext, BlobType.RESPONSE, servletResponse.getContentType());
        } else {
            log.debug("skip blob logging for server request/response as blob condition has failed");
        }
    }

    private void write(Object data, SpanBlobContext blobContext, BlobType blobType, String contentType) {
        if (data instanceof byte[]) {
            final BlobWriter writer = factory.create(blobContext);
            final BlobContent blob = new BlobContent((byte[]) data, contentType, blobType);
            BlobWriteHelper.writeBlob(writer, blob);
        } else {
            log.error("Fail to write server {} as blob in the span", blobType.getType());
        }
    }
}
