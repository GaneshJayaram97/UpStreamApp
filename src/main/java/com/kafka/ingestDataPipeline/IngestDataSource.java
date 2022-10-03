/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.ingestDataPipeline;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author gjayaraman
 * Sep 18, 2022
 */
public class IngestDataSource
{
    RestHighLevelClient elasticsearchClient;
    KafkaTemplate kafkaTemplate;
    AdminClient adminClient;
    String source_index;
    String topic;

    public IngestDataSource(RestHighLevelClient elasticsearchClient, KafkaTemplate kafkaTemplate, AdminClient adminClient, String source_index, String topic) {
        this.elasticsearchClient = elasticsearchClient;
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
        this.source_index = source_index;
        this.topic = topic;
    }

    public boolean isKafkaTopicPresent() {
        try {
            return adminClient.listTopics().names().get().stream().anyMatch(topic -> topic.equalsIgnoreCase(this.topic));
        }
        catch (Exception ex) {
            return false;
        }
    }

    public boolean isElasticReady() {
        try {
            ClusterHealthRequest request = new ClusterHealthRequest();
            request.waitForYellowStatus();
            ClusterHealthResponse response = elasticsearchClient.cluster().health(request, RequestOptions.DEFAULT);
            return !response.isTimedOut() && !ClusterHealthStatus.RED.name().equalsIgnoreCase(response.getStatus().name());
        }
        catch (Exception ex) {
            System.out.println("Exception while checking the ES readiness : " + ex);
            return false;
        }
    }

    public void assertElasticReady() throws InterruptedException {
        while (!isElasticReady()) {
            System.out.println("ES is not up and running or not ready. Retrying after a second");
            Thread.sleep(1000);
        }
    }

    public void startUpStreaming() throws IOException, InterruptedException {
        if (isKafkaTopicPresent()) {
            System.out.println("Topic is already present and hence not ingesting the data");
            return;
        }

        assertElasticReady();

        SearchRequest searchRequest = new SearchRequest(source_index);
        searchRequest.source().size(10000);
        searchRequest.scroll(TimeValue.timeValueMinutes(1));
        SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = response.getScrollId();
        SearchHit[] searchHits = response.getHits().getHits();

        System.out.println("Starting upstream with scroll : " + scrollId + " search hits : " + searchHits);
        while (searchHits != null && searchHits.length > 0) {

            send(searchHits);
            if (scrollId == null) {
                break;
            }
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1));
            response = elasticsearchClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            searchHits = response.getHits().getHits();
        }
    }

    void send(SearchHit[] searchHits) {
        // ingest into topic
        Arrays.stream(searchHits).forEach(stream -> {
            send(stream.getSourceAsMap());
        });
    }

    void send(Map<String, Object> source) {
        kafkaTemplate.send(topic, source).addCallback(new ListenableFutureCallback<SendResult<String, String>>()
        {
            @Override
            public void onSuccess(final SendResult<String, String> result) {
                System.out.println("Successfully sent the request for topic : " + topic +  " : " + result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(final Throwable ex) {
                System.out.println("Exception occurred while ingesting the messages to topic : " + topic + ", Exception : " + ex);
            }
        });
    }
}
