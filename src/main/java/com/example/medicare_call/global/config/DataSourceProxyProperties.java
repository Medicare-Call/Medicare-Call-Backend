package com.example.medicare_call.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.datasource-proxy")
public class DataSourceProxyProperties {

    // 프록시 적용에서 제외할 DataSource Bean 이름 목록
    private List<String> exclude = new ArrayList<>();
}
