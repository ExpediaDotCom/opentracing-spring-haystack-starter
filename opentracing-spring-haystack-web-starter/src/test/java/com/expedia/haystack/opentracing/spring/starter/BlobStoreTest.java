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

package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.blobs.core.BlobStore;
import com.expedia.blobs.core.BlobWriterImpl;
import com.expedia.blobs.model.Blob;
import com.expedia.haystack.blobs.spring.starter.Blobable;
import com.expedia.haystack.opentracing.spring.starter.model.TestEmployee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(
        properties = "spring.application.name=SimpleServer",
        classes = { SimpleServer.class, BlobStoreTest.TestContextConfiguration.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class BlobStoreTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private InMemoryBlobStore inMemoryBlobStore;

    @Test
    public void blobResponseTest() throws Exception {
        final String expectedHttpResponse = "Hello, World!";
        final String response = testRestTemplate.getForObject("/helloWorld", String.class);
        assertThat(response).isEqualTo(expectedHttpResponse);
        Thread.sleep(500);
        final Blob blob = inMemoryBlobStore.capturedBlobs.stream()
                .filter(b -> b.getContent().toStringUtf8().equalsIgnoreCase("Hello, World!"))
                .findFirst().get();
        assertThat(blob.getMetadataOrDefault("blob-type", "")).isEqualTo("response");
        assertThat(blob.getMetadataOrDefault("content-type", "")).contains(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void blobRequestTest() throws Exception {
        final TestEmployee emp = new TestEmployee("Alice");
        final ResponseEntity<TestEmployee> response = testRestTemplate.postForEntity("/post", emp, TestEmployee.class);

        final Blob postRequestBlob = inMemoryBlobStore.capturedBlobs.stream()
                .filter(b -> b.getContent().toStringUtf8().contains("name"))
                .filter(b -> b.getMetadataMap().getOrDefault("blob-type", "").equals("request"))
                .findFirst().get();

        final Blob postResponseBlob = inMemoryBlobStore.capturedBlobs.stream()
                .filter(b -> b.getContent().toStringUtf8().contains("name"))
                .filter(b -> b.getMetadataMap().getOrDefault("blob-type", "").equals("response"))
                .findFirst().get();

        final TestEmployee requestEntity = mapper.readValue(postRequestBlob.getContent().toStringUtf8(), TestEmployee.class);
        final TestEmployee responseEntity = mapper.readValue(postResponseBlob.getContent().toStringUtf8(), TestEmployee.class);

        Thread.sleep(500);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getName()).isEqualTo("Alice");

        assertThat(requestEntity.getName()).isEqualTo("Alice");
        assertThat(requestEntity.getId()).isEqualTo(0);
        assertThat(postRequestBlob.getMetadataOrDefault("content-type", "")).contains(MediaType.APPLICATION_JSON_VALUE);

        assertThat(responseEntity.getId()).isEqualTo(1);
        assertThat(responseEntity.getName()).isEqualTo("Alice");
        assertThat(postResponseBlob.getMetadataOrDefault("content-type", "")).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void skipBlobTest() throws Exception {
        final String response = testRestTemplate.getForObject("/redirect", String.class);
        Thread.sleep(500);
        final long cnt = inMemoryBlobStore.capturedBlobs.stream()
                .filter(b -> b.getContent().toStringUtf8().contains(response)).count();
        assertThat(cnt).isEqualTo(0);
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public InMemoryBlobStore blobStore() {
            return new BlobStoreTest.InMemoryBlobStore();
        }

        @Bean
        public Blobable testBlobable() {
            return new BlobStoreTest.TestBlobale();
        }
    }

    static class TestBlobale implements Blobable {
        public boolean isServerReqRespValidForBlob(final HttpServletRequest req,
                                                   final HttpServletResponse resp,
                                                   final Throwable servletException) {
            return resp.getStatus() != HttpStatus.PERMANENT_REDIRECT.value();
        }

        public boolean isClientReqRespValidForBlob(final HttpRequest req,
                                            @Nullable final ClientHttpResponse resp,
                                            @Nullable final Throwable throwable) {
            return false;
        }
    }

    static class InMemoryBlobStore implements BlobStore {
        final List<Blob> capturedBlobs = new ArrayList<>();

        @Override
        public void store(BlobWriterImpl.BlobBuilder blobBuilder) {
            capturedBlobs.add(blobBuilder.build());
        }

        @Override
        public Optional<Blob> read(String s) {
            return null;
        }

        @Override
        public void read(String s, BiConsumer<Optional<Blob>, Throwable> biConsumer) {
        }

        @Override
        public Optional<Blob> read(String s, long timeout, TimeUnit timeUnit) {
            return null;
        }
    }
}
