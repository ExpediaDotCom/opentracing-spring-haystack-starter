package com.expedia.haystack.blobs.spring.starter.decorators;

import com.expedia.blobs.core.BlobContext;
import com.expedia.blobs.core.BlobType;
import com.expedia.blobs.core.BlobWriter;
import com.expedia.blobs.core.BlobsFactory;
import com.expedia.haystack.blobs.SpanBlobContext;
import com.expedia.haystack.blobs.spring.starter.Blobable;
import com.expedia.haystack.blobs.spring.starter.model.BlobContainer;
import com.expedia.haystack.blobs.spring.starter.model.BlobContent;
import com.expedia.haystack.blobs.spring.starter.utils.BlobWriteHelper;
import io.opentracing.Span;
import io.opentracing.contrib.spring.web.client.RestTemplateSpanDecorator;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public class ClientRequestBlobDecorator implements RestTemplateSpanDecorator {

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
            write(container.getRequest(), blobContext, BlobType.REQUEST, MediaType.ALL_VALUE);
            write(container.getResponse(), blobContext, BlobType.RESPONSE, httpResponse.getHeaders().getContentType().getType());
        }
    }

    private void write(byte[] data, SpanBlobContext blobContext, BlobType blobType, String contentType) {
        final BlobWriter writer = factory.create(blobContext);
        final BlobContent blob = new BlobContent(data, contentType, blobType);
        BlobWriteHelper.writeBlob(writer, blob);
    }
}
