package com.expedia.haystack.opentracing.spring.starter.support;

import com.expedia.www.haystack.client.Tracer;

public interface TracerCustomizer {
    void customize(Tracer.Builder builder);
}
