# UpStream using Kafka

Spring application which upstreams the data from Elasticsearch index to Kafka 

Here, Kafka acts as a data pipeline where it can be consumed by [this](https://github.com/GaneshJayaram97/DownStreamApp) Spring application

Data can be ingested to elasticsearch and kafka through REST API exposed. API details mentioned on the usage section below 

All the components required for installing / running this application has been packaged in a docker-compose file and details on achieving the same is present in Usage Section

## Pre-requisites

### Docker

   Docker should be installed in the host machine
   ```
   https://docs.docker.com/get-docker/
   ```

### Maven
Maven should be installed in the host machine to build the application jar. 

Recommended version for this application is >= 3.6.3

It is recommended to add the maven binaries to $PATH environment variable so that maven commands can be executed without specifying their full path

## Usage

Below are the steps to build/create containers.

Execute the below commands from the source directory of the project

1. Build the application jar by running the below command
    ```
    mvn clean install
    ```

2. Build and Run the containers 
   
    a. Start Kafka either as a standalone mode or cluster mode
      
      i. To start Kafka as Standalone mode
      ```
      docker-compose -f kafka-docker-compose.yml up
      ```
     ii. To start kafka as Cluster mode
     ```
     docker-compose -f kafka-cluster-docker-compose.yml up
     ``` 

   b. Start Elasticsearch and Spring application containers 
   ```
   docker-compose up
   ``` 

3. REST API
     
     Exposed in 8080 port and can be accessed via localhost hostname
     
     Below snippet is a sample ingestion of data into elasticsearch index and into kafka topic
  
      Elastic Index and Kafka Topic can be configured in application.properties 

    ```
    curl --location --request POST "http://localhost:8080/bulkIngest" \
     --header 'Content-Type: application/json' \
     --data-raw '[{
         "@timestamp": "2022-09-15T13:12:00",
         "message": "GET /search HTTP/1.1 200 1070000",
         "user": {
            "id": "Kimchy"
         }
      },
      {
        "@timestamp": "2022-11-15T13:12:00",
        "message": "GET /update HTTP/1.1 200 1070000",
        "user": {
           "id": "Kimchy"
        }
      }
    ]'
    ```


## Notes

 1. Parameters such as elasticsearch index, host, port, kafka bootstrap servers, topic, partition, replicas are configured in application.properties under src/main/resources directory. Update them based on the need and build the jar and re-deploy the containers as needed. 
 2. By default, application would up stream all the data present in the elasticsearch.source.index variable mentioned in application.properties into kafka topic (configured with variable kafka.topic in application.properties) during bootup, if the topic is not present
 3. All containers are configured to run under a common network configuration and its definition is present in kafka-docker-compose.yml/kafka-cluster-docker-compose.yml files. Hence, start the kafka containers before starting any other containers during the first time
 4. Kafka topic's partition and replica can be adjusted / tweaked based on the need. 

## Contributing
This is a bootstrap application for streaming the data into kafka topic. Contributions to further enhance this application are always welcomed.


## License