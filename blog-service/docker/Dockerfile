FROM openjdk:8-jre-slim-buster
ENV TZ "Asia/Shanghai"
ENV JAVA_OPTS "-XX:+UseSerialGC -XX:MaxRAM=100m -Xss512k"
RUN "ls -a"
COPY blog-service-0.0.1-SNAPSHOT.jar /usr/local/lib/blog-service.jar
EXPOSE 8084
ENTRYPOINT exec java $JAVA_OPTS -jar /usr/local/lib/blog-service.jar