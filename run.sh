#!/bin/bash 
jar=`ls target/cassandra*.jar`
yaml=`ls target/app*.yml`

echo "java -jar $jar  --spring.config.name=$yaml"
java -jar $jar  --spring.config.name=$yaml