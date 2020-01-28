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

package com.expedia.haystack.blobs.spring.starter.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BlobFilter implements Filter {
    private final static Logger log = LoggerFactory.getLogger(BlobFilter.class);
    public static final String REQUEST_BLOB_KEY = BlobFilter.class.getName() + ".request";
    public static final String RESPONSE_BLOB_KEY = BlobFilter.class.getName() + ".response";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        log.debug("Adding " + BlobFilter.class.getSimpleName() + " to server filters");
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        final BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest);

        httpRequest.setAttribute(REQUEST_BLOB_KEY, bufferedRequest.getBuffer());

        final ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
        final HttpServletResponse wrappedResp = new HttpServletResponseWrapper(httpResponse) {
            public PrintWriter getWriter() {
                return pw.getWriter();
            }
            public ServletOutputStream getOutputStream() {
                return pw.getStream();
            }
        };

        try {
            chain.doFilter(bufferedRequest, wrappedResp);
        } finally {
            if (httpRequest.isAsyncStarted()) {
                httpRequest.getAsyncContext()
                        .addListener(new AsyncListener() {
                            @Override
                            public void onComplete(AsyncEvent event) throws IOException {
                                assignBlobAttribute(pw, httpRequest, httpResponse);
                            }

                            @Override
                            public void onTimeout(AsyncEvent event) throws IOException {
                                assignBlobAttribute(pw, httpRequest, httpResponse);

                            }

                            @Override
                            public void onError(AsyncEvent event) throws IOException {
                                assignBlobAttribute(pw, httpRequest, httpResponse);
                            }

                            @Override
                            public void onStartAsync(AsyncEvent event) throws IOException {
                            }
                        });
            } else {
                assignBlobAttribute(pw, httpRequest, httpResponse);
            }
        }
    }

    private void assignBlobAttribute(ByteArrayPrintWriter pw,
                                     HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) throws IOException {
        final byte[] bytes = pw.toByteArray();
        httpResponse.getOutputStream().write(bytes);
        httpRequest.setAttribute(RESPONSE_BLOB_KEY, bytes);
    }

    @Override
    public void destroy() {
    }


    private class BufferedRequestWrapper extends HttpServletRequestWrapper {
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        BufferedServletInputStream bsis;

        byte[] buffer;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
            baos = new ByteArrayOutputStream();
            IOUtils.copy(req.getInputStream(), baos);
            buffer = baos.toByteArray();
        }

        public ServletInputStream getInputStream() {
            bais = new ByteArrayInputStream(buffer);
            bsis = new BufferedServletInputStream(bais);
            return bsis;
        }

        public byte[] getBuffer() {
            return buffer;
        }
    }

    private class BufferedServletInputStream extends ServletInputStream {

        ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        public int available() {
            return bais.available();
        }

        public int read() {
            return bais.read();
        }

        public int read(byte[] buf, int off, int len) {
            return bais.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return bais.available() <= 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }

    private static class ByteArrayPrintWriter {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private PrintWriter pw = new PrintWriter(baos);

        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private static class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;
        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
