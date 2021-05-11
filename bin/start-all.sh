#!/usr/bin/env bash
java_version=$(java -version 2>&1 >/dev/null | grep 'java version' | awk '{print $3}')
if [ -z "$java_version" ]; then
  ecddho "JAVA NOT FOUND IN SYSTEM"
else
  echo "FIND JAVA VERSION $java_version INSTALL IN ${JAVA_HOME}"
fi
echo
#profile settings
while getopts ":p:d" opt; do
  case $opt in
  p)
    profile="$OPTARG"
    ;;
  d)
    deploy="$OPTARG"
    ;;
  \?)
    echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done
declare -a valid_profiles=("dev" "prod")
valid_flag=false
if [ -z "$profile" ]; then
  profile=dev
  echo "ACTIVE PROFILE NOT SET,USE dev PROFILE"
else
  for i in "${valid_profiles[@]}"; do
    if [ "$profile" = "$i" ]; then
      valid_flag=true
      break
    elif [ "$profile" = "$i" ]; then
      valid_flag=true
      break
    fi
  done
  if $valid_flag; then
    echo "PROFILE SET AS $profile"
  else
    profile="dev"
    echo "PROFILE IS INVALID,USE DEFAULT dev PROFILE"
  fi
fi
#Maven package
if [ -z ${deploy+x} ]; then
  #deploy mode no need to package
  echo "NATIVE MODE,START TO PACKAGE PROJECT"
  cd ..
  mvn clean package -Dmaven.test.skip=true
  cd bin || exit
else
  echo "DEPLOY MODE,NO NEED TO PACKAGE PROJECT"
fi

#Start up Config Server
echo "START UP CONFIG SERVER"
cd ../config/target || exit
jar -xf config-0.0.1-SNAPSHOT.jar
java -Dname=CONFIG -Dfile.encoding=UTF-8 -Xms64m -Xmx80m -Dspring.profiles.active=$profile org.springframework.boot.loader.JarLauncher >~/config-log.txt 2>~/config-errors.txt </dev/null &
PID=$!
echo $PID >config-pid.txt
sleep 8
config_status=$(jps -v | grep CONFIG)
if [ -z "$config_status" ]; then
  echo "CONFIG-SERVER START UP FAIL"
else
  echo "CONFIG-SERVER START UP SUCCESS"
fi
echo
#Start up Eureka Server
echo "START UP DISCOVERY"
cd ../../service-discovery/target || exit
jar -xf service-discovery-0.0.1-SNAPSHOT.jar
java -Dname=DISCOVERY -Dfile.encoding=UTF-8 -Xms60m -Xmx95m -Dspring.profiles.active=$profile org.springframework.boot.loader.JarLauncher >~/eureka-log.txt 2>~/eureka-errors.txt </dev/null &
PID=$!
echo $PID >eureka-pid.txt
sleep 45
eureka_status=$(jps -v | grep DISCOVERY)
if [ -z "$eureka_status" ]; then
  echo "DISCOVERY SERVER START UP FAIL"
else
  echo "DISCOVERY SERVER START UP SUCCESS"
fi
echo
#wait eureka register config server
#Start up Gateway Server
echo "START UP GATEWAY"
cd ../../gateway/target || exit
jar -xf gateway-0.0.1-SNAPSHOT.jar
java -Dname=GATEWAY -Dfile.encoding=UTF-8 -Xms64m -Xmx80m -Dspring.profiles.active=$profile org.springframework.boot.loader.JarLauncher >~/gateway-log.txt 2>~/gateway-errors.txt </dev/null &
PID=$!
echo $PID >gateway-pid.txt
sleep 5
gateway_status=$(jps -v | grep GATEWAY)
if [ -z "$gateway_status" ]; then
  echo "GATEWAY SERVER START UP FAIL"
else
  echo "GATEWAY SERVER START UP SUCCESS"
fi
echo
#Start up Blog-Service Server
echo "START UP BLOG SERVICE"
cd ../../blog-service/target || exit
jar -xf blog-service-0.0.1-SNAPSHOT.jar
java -Dname=BLOG-SERVICE -Dfile.encoding=UTF-8 -Xms250m -Xmx400m -Dspring.profiles.active=$profile org.springframework.boot.loader.JarLauncher >~/blog-service-log.txt 2>~/blog-service-errors.txt </dev/null &
PID=$!
echo $PID >blog-service-pid.txt
sleep 8
blog_service_status=$(jps -v | grep BLOG-SERVICE)
if [ -z "$blog_service_status" ]; then
  echo "BLOG-SERVICE START UP FAIL"
else
  echo "BLOG-SERVICE START UP SUCCESS"
fi
echo
