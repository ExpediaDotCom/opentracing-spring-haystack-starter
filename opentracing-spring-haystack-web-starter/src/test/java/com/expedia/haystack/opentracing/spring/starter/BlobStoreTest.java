package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.blobs.core.BlobStore;
import com.expedia.blobs.core.BlobWriterImpl;
import com.expedia.blobs.model.Blob;
import com.expedia.haystack.opentracing.spring.starter.model.TestEmployee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

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
        Assertions.assertThat(response).isEqualTo(expectedHttpResponse);
        Thread.sleep(500);
        assertThat(inMemoryBlobStore.helloWorldResponseBlob.getContent().toStringUtf8()).isEqualTo(expectedHttpResponse);
        assertThat(inMemoryBlobStore.helloWorldResponseBlob.getMetadataOrDefault("content-type", ""))
                .contains(MediaType.TEXT_PLAIN_VALUE);
    }

    @Test
    public void blobRequestTest() throws Exception {
        final TestEmployee emp = new TestEmployee("Alice");
        final ResponseEntity<TestEmployee> response = testRestTemplate.postForEntity("/post", emp, TestEmployee.class);
        final TestEmployee requestEntity = mapper.readValue(inMemoryBlobStore.postRequestBlob.getContent().toStringUtf8(), TestEmployee.class);
        final TestEmployee responseEntity = mapper.readValue(inMemoryBlobStore.postResponseBlob.getContent().toStringUtf8(), TestEmployee.class);

        Thread.sleep(500);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody().getName()).isEqualTo("Alice");
        Assertions.assertThat(requestEntity.getName()).isEqualTo("Alice");
        Assertions.assertThat(requestEntity.getId()).isEqualTo(0);

        Assertions.assertThat(responseEntity.getId()).isEqualTo(1);
        Assertions.assertThat(responseEntity.getName()).isEqualTo("Alice");
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public InMemoryBlobStore blobStore() {
            return new BlobStoreTest.InMemoryBlobStore();
        }
    }

    static class InMemoryBlobStore implements BlobStore {
        Blob helloWorldResponseBlob;
        Blob postRequestBlob;
        Blob postResponseBlob;

        @Override
        public void store(BlobWriterImpl.BlobBuilder blobBuilder) {
            final Blob blob = blobBuilder.build();
            final String blobContent = blob.getContent().toStringUtf8();
            final String blobType = blob.getMetadataMap().getOrDefault("blob-type", "");

            if(blobContent.contains("name")) {
                if (blobType.equalsIgnoreCase("REQUEST")) {
                    postRequestBlob =  blob;
                } else {
                    postResponseBlob = blob;
                }
            } else if (blobContent.contains("Hello") && blobType.equalsIgnoreCase("RESPONSE")) {
                helloWorldResponseBlob = blob;
            }
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
