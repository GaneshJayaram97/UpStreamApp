/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.ingestDataPipeline;

import java.io.IOException;
import java.util.Collections;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gjayaraman
 * Sep 18, 2022
 */
@Configuration
public class IngestDataSourceConfigurations implements ApplicationListener<ApplicationReadyEvent>
{

    @Autowired
    SpringKafkaConfigurations springKafkaConfigurations;

    @Autowired
    SpringElasticConfigurations springElasticConfigurations;

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(springKafkaConfigurations.getDefaultProperties());
    }

    @Bean
    IngestDataSource upStreamDataSource() {
        return new IngestDataSource(springElasticConfigurations.restHighLevelClient(), springKafkaConfigurations.kafkaTemplate(), adminClient(), springElasticConfigurations.SOURCE_INDEX, springKafkaConfigurations.TOPIC);
    }

    @Bean
    public void createTopic() {
        adminClient().createTopics(Collections.singleton(new NewTopic(springKafkaConfigurations.TOPIC, springKafkaConfigurations.TOPIC_PARTITION, (short) springKafkaConfigurations.TOPIC_REPLICA)));
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            createTopic();
            upStreamDataSource().startUpStreaming();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
