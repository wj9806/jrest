package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.client.JRestClientFactory;

@FunctionalInterface
public interface JRestClientFactoryBuilderConfigure {

    void configure(JRestClientFactory.Builder builder);

}
