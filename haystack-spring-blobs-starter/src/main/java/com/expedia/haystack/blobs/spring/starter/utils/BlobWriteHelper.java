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

package com.expedia.haystack.blobs.spring.starter.utils;

import com.expedia.blobs.core.BlobWriter;
import com.expedia.haystack.blobs.spring.starter.model.BlobContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class BlobWriteHelper {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlobWriteHelper.class);

    public static void writeBlob(final BlobWriter blobWriter,
                                 final BlobContent blob,
                                 final Map<String, String> metadata) {
        blobWriter.write(
                blob.getBlobType(),
                blob.getContentType(),
                (outputStream) -> {
                    try {
                        outputStream.write(blob.getData());
                    } catch (IOException e) {
                        LOGGER.error("Exception occurred while writing data to stream for preparing blob", e);
                    }
                },
                md -> {
                    metadata.forEach(md::add);
                }
        );
    }
}