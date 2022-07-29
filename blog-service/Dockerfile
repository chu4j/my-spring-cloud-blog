# Build Stage
FROM blog-service-base:1.0 as build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /usr/share/maven/ref/
RUN mkdir -p /root/markdown
RUN mvn -B -f /home/app/pom.xml -s /usr/share/maven/ref/settings.xml clean package -Dmaven.test.skip -DskipTests -Dspring.profiles.active=dev
# Package Stage
FROM openjdk:8-jre-slim-buster
ENV TZ "Asia/Shanghai"
ENV JAVA_OPTS "-XX:+UseSerialGC -XX:MaxRAM=100m -Xss512k"
COPY --from=build /home/app/target/blog-service-0.0.1-SNAPSHOT.jar /usr/local/lib/blog-service.jar
EXPOSE 8084
ENTRYPOINT exec java $JAVA_OPTS -jar /usr/local/lib/blog-service.jar
