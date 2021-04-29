# Kafka Binder Tests
A standalone Spring application that runs functional tests of Spring Cloud Stream Binder Kafka. Created for the ISM6145 course at the University of South Florida in the Spring 2021 semester.

## Setup
Build the Linux Docker image and the executable JAR of the application:
```
$ cd kafka-binder-tests
$ docker image build -t debian/default-jre .
$ mvn clean package
```
Initialize the testing environment:
```
docker-compose up -d
```
Enter the Linux Docker container (if using Git for Windows, prepend with `winpty`:
```
docker exec -it kafka-binder-tests_debian_1 bash
```
In the container, execute the JAR:
```
root@6e88f2084bff:/# java -jar target/mborkland-0.0.1-SNAPSHOT.jar
```
After the application finishes and exits, view the log summary:
```
root@6e88f2084bff:/# cat /tmp/logs.txt
```

## Issues
* The `topicHasWrongObjectTypeRiskTest` test issues a false negative because it is unable to programmatically detect that an expected exception is thrown. I tried overriding different `MessageConverter` beans to set a flag in the `TestContext`, but I couldn't get it to work without breaking the other tests.
* I could not get the configuration right for the dead letter queue. All of the examples online still use the deprecated imperative method instead of the functional method that is now preferred.

Please feel free to submit a PR with any fixes or enhancements.