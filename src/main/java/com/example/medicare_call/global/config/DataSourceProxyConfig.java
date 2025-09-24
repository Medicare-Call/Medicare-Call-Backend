package com.example.medicare_call.global.config;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    @Primary
    public DataSource proxyDataSource(DataSourceProperties properties) {
        DataSource realDataSource = properties.initializeDataSourceBuilder().build();

        return ProxyDataSourceBuilder
                .create(realDataSource)
                .name("DS-Proxy")
                .listener(new CustomQueryExecutionListener())
                .build();
    }
}