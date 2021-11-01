#!/usr/bin/env bash
#Stop config service
echo "STOP CONFIG SERVER... ..."
cd ../config/target || exit
CONFIG_PID=$(cat config-pid.txt)
kill -9 "$CONFIG_PID"
CONFIG_STATUS=$(jps -v | grep CONFIG)
if [ -z "$CONFIG_STATUS" ]; then
  printf "%s\n" "STOP CONFIG SERVER DONE"
else
  printf "STOP CONFIG SERVER FAIL\n"
fi
echo
#Stop Eureka
echo "STOP DISCOVERY SERVER... ..."
cd ../../service-discovery/target || exit
DISCOVERY_PID=$(cat eureka-pid.txt)
kill -9 "$DISCOVERY_PID"
DISCOVERY_STATUS=$(jps -v | grep DISCOVERY)
if [ -z "$DISCOVERY_STATUS" ]; then
  printf "STOP DISCOVERY SERVER DONE\n"
else
  printf "STOP DISCOVERY SERVER FAIL\n"
fi
echo
#Stop gateway
echo "STOP GATEWAY SERVER... ..."
cd ../../gateway/target || exit
GATEWAY_PID=$(cat gateway-pid.txt)
kill -9 "$GATEWAY_PID"
GATEWAY_STATUS=$(jps -v | grep GATEWAYY)
if [ -z "$GATEWAY_STATUS" ]; then
  printf "STOP GATEWAY SERVER DONE\n"
else
  printf "STOP GATEWAY SERVER FAIL\n"
fi
echo
#Stop blog service
echo "STOP BLOG SERVICE SERVER... ..."
cd ../../blog-service/target || exit
BLOG_SERVICE_PID=$(cat blog-service-pid.txt)
kill -9 "$BLOG_SERVICE_PID"
BLOG_SERVICE_STATUS=$(jps -v | grep BLOG-SERVICE)
if [ -z "$BLOG_SERVICE_STATUS" ]; then
  printf "STOP BLOG SERVICE SERVER DONE\n"
else
  printf "STOP BLOG SERVICE SERVER FAIL\n"
fi
echo
