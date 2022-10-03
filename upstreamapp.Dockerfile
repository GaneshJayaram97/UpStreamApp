FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.9.1_1
RUN addgroup -S spring && adduser -S spring -G spring
ARG JAR_FILE=target/*.jar
ARG TARGET_DIR=/opt/upstreamapp/target
ARG JAR_FILE_CONTAINER_PATH=${TARGET_DIR}/app.jar
RUN mkdir -p ${TARGET_DIR}
RUN chown spring:spring ${TARGET_DIR}
USER spring:spring
COPY ${JAR_FILE} ${JAR_FILE_CONTAINER_PATH}
ENV JAR_FILE_CONTAINER_PATH_ENV=${JAR_FILE_CONTAINER_PATH}
ENTRYPOINT java -jar ${JAR_FILE_CONTAINER_PATH_ENV}
