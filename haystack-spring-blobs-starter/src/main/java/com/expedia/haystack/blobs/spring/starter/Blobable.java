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

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Blobable {
    Blobable DEFAULT_BLOBABLE = new Blobable() {};

    /**
     * if you want to control blob logging based based on the request or response, then you can override this method.
     * Return true to turn on blob else return false. Default behavior is true.
     * One usecase is to blob only if response fails or servlet exception is thrown
     * @param req http request
     * @param resp http response
     * @param servletException servlet exception if thrown
     * @return
     */
    default boolean isServerReqRespValidForBlob(final HttpServletRequest req,
                                                final HttpServletResponse resp,
                                                @Nullable final Throwable servletException) {
        return true;
    }

    default boolean isClientReqRespValidForBlob(final HttpRequest req,
                                                @Nullable final ClientHttpResponse resp,
                                                @Nullable final Throwable throwable) {
        return true;
    }
}