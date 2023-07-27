#!/bin/bash
# exec: `sh nacos_config_import.sh 192.168.56.102:31053`
if [ $# != 1 ]; then
  echo "<host>:<port> not provide!"
  exit -1
fi
addr=$1
for f in `ls */src/main/resources/application.local`
do 
  app=${f%%/*}
  text=`cat $f`
  result=`curl -X POST "http://$addr/nacos/v1/cs/configs" -d "dataId=$app.properties&group=DEFAULT_GROUP&content=$text&type=properties"`
  echo $app $result
done