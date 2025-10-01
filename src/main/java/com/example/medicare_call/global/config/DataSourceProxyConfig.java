package com.example.medicare_call.global.config;

import com.example.medicare_call.global.metrics.CustomQueryExecutionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    public static BeanPostProcessor dataSourceProxyBeanPostProcessor(
            CustomQueryExecutionListener listener,
            DataSourceProxyProperties properties
    ) {
        return new DataSourceProxyBeanPostProcessor(listener, properties.getExclude());
    }

    @Slf4j
    @RequiredArgsConstructor
    static class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {
        private final CustomQueryExecutionListener listener;
        private final List<String> excludedBeanNames;

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource) {
                // 이미 ProxyDataSource인 경우 재프록시 방지
                if (bean instanceof ProxyDataSource) {
                    log.info("Skip proxy: DataSource [{}] is already a ProxyDataSource", beanName);
                    return bean;
                }

                // application.yml 설정에서 제외된 DataSource인 경우 스킵
                if (excludedBeanNames.contains(beanName)) {
                    log.info("Skip proxy: DataSource [{}] is excluded by configuration", beanName);
                    return bean;
                }

                // Proxy 적용
                log.info("Apply proxy: Wrapping DataSource [{}] with ProxyDataSource", beanName);
                return ProxyDataSourceBuilder
                        .create((DataSource) bean)
                        .name("DS-Proxy")
                        .listener(listener)
                        .build();
            }
            return bean;
        }
    }
}