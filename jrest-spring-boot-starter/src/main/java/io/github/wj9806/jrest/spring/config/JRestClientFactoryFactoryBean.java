package io.github.wj9806.jrest.spring.config;

import io.github.wj9806.jrest.client.JRestClientFactory;
import io.github.wj9806.jrest.client.http.CodecManager;
import io.github.wj9806.jrest.client.http.Retryer;
import io.github.wj9806.jrest.client.http.decode.Decoder;
import io.github.wj9806.jrest.client.http.encode.Encoder;
import io.github.wj9806.jrest.client.interceptor.HttpRequestInterceptor;
import io.github.wj9806.jrest.client.annotation.AnnotationParser;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JRestClientFactoryFactoryBean implements FactoryBean<JRestClientFactory>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public JRestClientFactory getObject() throws Exception {

        JRestClientFactory.Builder builder = new JRestClientFactory.Builder();

        AnnotationParser parser = applicationContext.getBeanProvider(AnnotationParser.class).getIfAvailable();
        Retryer retryer = applicationContext.getBeanProvider(Retryer.class).getIfAvailable();
        CodecManager codecManager = applicationContext.getBeanProvider(CodecManager.class).getIfAvailable();

        builder.annotationParser(parser)
                .retryer(retryer)
                .codecManager(codecManager);

        applicationContext.getBeanProvider(Encoder.class).forEach(builder::addEncoder);
        applicationContext.getBeanProvider(Decoder.class).forEach(builder::addDecoder);
        applicationContext.getBeanProvider(HttpRequestInterceptor.class)
                .forEach(builder::addInterceptor);

        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return JRestClientFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
