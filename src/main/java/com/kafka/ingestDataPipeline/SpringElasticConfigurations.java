/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.ingestDataPipeline;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gjayaraman
 * Sep 18, 2022
 */
@Configuration
public class SpringElasticConfigurations
{

    @Value("${elasticsearch.source.index}")
    public String SOURCE_INDEX;

    @Value("${elasticsearch.host}")
    private String elasticSearchHost;

    @Value("${elasticsearch.port}")
    private int elasticSearchPort;

    @Bean
    RestClient restClient() {
        return RestClient.builder(
                new HttpHost(elasticSearchHost, elasticSearchPort)).build();
    }

    @Bean
    RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClientBuilder(restClient())
                .setApiCompatibilityMode(true)
                .build();
    }
}
