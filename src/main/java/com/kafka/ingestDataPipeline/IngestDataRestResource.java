/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.ingestDataPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjayaraman
 * Oct 01, 2022
 */
@RestController
public class IngestDataRestResource
{

    @Autowired
    SpringElasticConfigurations springElasticConfigurations;

    @Autowired
    IngestDataSource ingestDataSource;

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED)
    public IndexResponse ingestData(@RequestBody Object data) throws IOException {
        Map<String, Object> map = (Map<String, Object>) data;
        IndexResponse response = springElasticConfigurations.restHighLevelClient().index(new IndexRequest(springElasticConfigurations.SOURCE_INDEX).source(map,
                XContentType.JSON), RequestOptions.DEFAULT);
        ingestDataSource.send(map);
        return response;
    }

    @PostMapping("/bulkIngest")
    @ResponseStatus(HttpStatus.CREATED)
    public List<IndexResponse> bulkIngestData(@RequestBody Object data) {
        List<Map<String, Object>> listOfMap = (List<Map<String, Object>>) data;
        List<IndexResponse> responses = new ArrayList<>();
        listOfMap.stream().forEach(map -> {
            try {
                responses.add(springElasticConfigurations.restHighLevelClient().index(new IndexRequest(springElasticConfigurations.SOURCE_INDEX).source(map,
                        XContentType.JSON), RequestOptions.DEFAULT));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            ingestDataSource.send(map);
        });
        return responses;
    }
}
