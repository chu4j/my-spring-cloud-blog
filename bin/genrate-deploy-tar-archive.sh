#!/usr/bin/env bash
#Delete deploy file
rm -rf ./my-spring-cloud-blog
#Try clean and package
cd ..
mvn clean package -Dmaven.test.skip=true
cd ./bin || exit
#Create deploy folder
echo "START TO GENERATE DEPLOY TAR ARCHIVE"
mkdir "my-spring-cloud-blog"
mkdir "./my-spring-cloud-blog/bin"
mkdir "./my-spring-cloud-blog/config"
mkdir "./my-spring-cloud-blog/config/target"
mkdir "./my-spring-cloud-blog/service-discovery"
mkdir "./my-spring-cloud-blog/service-discovery/target"
mkdir "./my-spring-cloud-blog/gateway"
mkdir "./my-spring-cloud-blog/gateway/target"
mkdir "./my-spring-cloud-blog/blog-service"
mkdir "./my-spring-cloud-blog/blog-service/target"
echo
echo "COPY DEPLOY JAR FILE"
#Copy jar
cp ../config/target/config-0.0.1-SNAPSHOT.jar ./my-spring-cloud-blog/config/target/config-0.0.1-SNAPSHOT.jar
cp ../service-discovery/target/service-discovery-0.0.1-SNAPSHOT.jar ./my-spring-cloud-blog/service-discovery/target/service-discovery-0.0.1-SNAPSHOT.jar
cp ../gateway/target/gateway-0.0.1-SNAPSHOT.jar ./my-spring-cloud-blog/gateway/target/gateway-0.0.1-SNAPSHOT.jar
cp ../blog-service/target/blog-service-0.0.1-SNAPSHOT.jar ./my-spring-cloud-blog/blog-service/target/blog-service-0.0.1-SNAPSHOT.jar
echo
echo "COPY DEPLOY SHELL SCRIPT"
#Copy shell script
cp start-all.sh ./my-spring-cloud-blog/bin/start-all.sh
cp stop-all.sh ./my-spring-cloud-blog/bin/stop-all.sh
cp start-blog-service.sh ./my-spring-cloud-blog/bin/start-blog-service.sh
tar -zcvf my-spring-cloud-blog.tar.gz ./my-spring-cloud-blog
echo
rm -rf ./my-spring-cloud-blog
echo
echo "GENERATE DEPLOY TAR ARCHIVE DONE..."
