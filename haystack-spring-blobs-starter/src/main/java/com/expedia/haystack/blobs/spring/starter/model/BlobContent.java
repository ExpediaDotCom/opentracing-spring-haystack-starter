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

package com.expedia.haystack.blobs.spring.starter.model;

import com.expedia.blobs.core.BlobType;
import com.expedia.blobs.core.ContentType;
import org.springframework.http.MediaType;

public class BlobContent {
    private final byte[] data;
    private final ContentType contentType;
    private final BlobType blobType;

    public BlobContent(final byte[] data, final String contentType, BlobType blobType) {
        this.data = data;
        this.contentType = contentType == null || contentType.isEmpty() ?
                ContentType.from(MediaType.TEXT_PLAIN_VALUE) : ContentType.from(contentType);
        this.blobType = blobType;
    }
    public byte[] getData() {
        return data;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public BlobType getBlobType() {
        return blobType;
    }
}
