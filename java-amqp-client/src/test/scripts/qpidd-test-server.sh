#!/bin/bash

qpidd -d # --log-to-stdout yes 

sleep 4

qpid-config add queue test
java -cp "../../../target/test-classes:../../../target/dependency/*:../../../target/java-amqp-client-1.0-SNAPSHOT.jar" \
com.redhat.osas.amqp.client.ReceiveOneMessage &
java -cp "../../../target/test-classes:../../../target/dependency/*:../../../target/java-amqp-client-1.0-SNAPSHOT.jar" \
com.redhat.osas.amqp.client.SendOneMessage
   
qpidd -q
