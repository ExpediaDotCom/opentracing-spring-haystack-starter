/*
 * Copyright 2018 Expedia, Inc.
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

import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = "spring.application.name=SimpleServer",
        classes = { SimpleServer.class, DispatcherInjectionIntegrationTest.TestContextConfiguration.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class DispatcherInjectionIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private InMemoryDispatcher inMemoryDispatcher;

    @Test
    public void testHaystackWebStarterWiresServerTracesToInjectedDispatcher() throws Exception {
        final String response = testRestTemplate.getForObject("/helloWorld", String.class);
        Assertions.assertThat(response).isEqualTo("Hello, World!");
        Thread.sleep(500);
        Assertions.assertThat(inMemoryDispatcher.getReportedSpans().size()).isEqualTo(2);
        Assertions.assertThat(
                inMemoryDispatcher.getReportedSpans()
                        .stream()
                        .anyMatch(span -> span.getTags().get("span.kind").equals("server"))).isEqualTo(true);
        inMemoryDispatcher.flush();
    }

    @Test
    public void testHaystackStarterWiresRestTemplateClientTracesToInjectedDispatcher() throws Exception {
        final String response = testRestTemplate.getForObject("/helloWorld", String.class);
        Assertions.assertThat(response).isEqualTo("Hello, World!");
        Thread.sleep(500);
        Assertions.assertThat(inMemoryDispatcher.getReportedSpans().size()).isEqualTo(2);
        Assertions.assertThat(
                inMemoryDispatcher.getReportedSpans()
                        .stream()
                        .anyMatch(span -> span.getTags().get("span.kind").equals("client"))).isEqualTo(true);
        inMemoryDispatcher.flush();
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public InMemoryDispatcher dispatcher(MetricsRegistry metricsRegistry) {
            return new InMemoryDispatcher.Builder(metricsRegistry)
                    .withLimit(10).build();
        }
    }
}
