package edu.usf.business.softwaretesting.mborkland.runner.test;

import com.example.Vehicle;
import edu.usf.business.softwaretesting.mborkland.model.NotAPerson;
import edu.usf.business.softwaretesting.mborkland.model.Person;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.IntStream;

import static edu.usf.business.softwaretesting.mborkland.util.TestUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;

@Component
public class FunctionalTestRunner {

    private static final Logger logger = Logger.getLogger("FunctionalTestRunner");

    private final TestContext testContext;

    private final KafkaTemplate<byte[], byte[]> template;

    private final String broker;

    private final String logPath;

    public FunctionalTestRunner(TestContext testContext, KafkaTemplate<byte[], byte[]> template, @Value("${kafka.config.uri}") String broker, @Value("${kafka.config.log-path}") String logPath) throws IOException {
        this.testContext = testContext;
        this.template = template;
        this.broker = broker;
        this.logPath = logPath;
        configureFileHandler();
    }

    public void runAllTests() {
        runTest(this::stringMessagesTest, "stringMessagesTest");
        standardSleep();
        runTest(this::objectMessageTest, "objectMessageTest");
        standardSleep();
        runTest(this::messagesInBatchTest, "messagesInBatchTest");
        standardSleep();
        runTest(this::deadLetterQueueTest, "deadLetterQueueTest");
        standardSleep();
        runTest(this::topicDoesNotExistRiskTest, "topicDoesNotExistRiskTest");
        standardSleep();
        runTest(this::topicHasWrongObjectTypeRiskTest, "topicHasWrongObjectTypeRiskTest");
        standardSleep();
        runTest(this::stringMessagesStressTest, "stringMessagesStressTest");
        standardSleep();
        runTest(this::objectMessagesStressTest, "objectMessagesStressTest");
        standardSleep();
        runTest(this::avroSchemaScenarioTest, "avroSchemaScenarioTest");
        standardSleep();
        runTest(this::partitionScenarioTest, "partitionScenarioTest");
    }

    private void stringMessagesTest() {
        basicStringTest(10);
    }

    private void objectMessageTest() {
        personTest(1);
    }

    private void messagesInBatchTest() {
        final int numMessages = 10;
        IntStream.range(0, numMessages).forEach(i -> template.send("batch-in", ("\"test" + i + "\"").getBytes()));
        standardSleep();
        assertThat(testContext.getMessages().size()).isEqualTo(numMessages);
        assertMessages(numMessages);
    }

    private void deadLetterQueueTest() {
        NotAPerson notAPerson = new NotAPerson(2000, 4, 2, 2, new Person("Mike", "Borkland", 33));
        template.send("notAPerson-in", notAPerson.toString().getBytes());
        template.send("notAPerson-in", new byte[]{Integer.valueOf(234).byteValue()});
        template.send("notAPerson-in", "not a notAPerson".getBytes());
        standardSleep();
        assertThat(testContext.getNotPeople().size()).isEqualTo(1);
        assertThat(testContext.getNotPeople().get(0)).isEqualTo(notAPerson);
        assertThat(testContext.getDeadLetterQueue().size()).isEqualTo(2);
    }

    private void topicDoesNotExistRiskTest() {
        Set<String> kafkaTopics = getKafkaTopics();
        String randomTopic;
        do {
            randomTopic = String.format("%s-in", RandomStringUtils.random(10, true, false));
        } while (kafkaTopics.contains(randomTopic));
        logger.log(Level.INFO, "Attempting to send message to random topic with name={0}", randomTopic);
        String finalRandomTopic = randomTopic;
        template.send(finalRandomTopic, "test".getBytes());
        kafkaTopics = getKafkaTopics();
        assertThat(kafkaTopics.contains(finalRandomTopic)).isTrue();
    }

    private void topicHasWrongObjectTypeRiskTest() {
        final Person person = new Person("Mike", "Borkland", 33);
        template.send("risk-in", person.toString().getBytes());
        standardSleep();
        assertThat(testContext.isExceptionThrown()).isTrue();
    }

    private void stringMessagesStressTest() {
        final int numMessages = 1000000;
        logger.log(Level.INFO, "Sending {0} string messages for stress test", numMessages);
        basicStringTest(numMessages);
    }

    private void objectMessagesStressTest() {
        final int numObjects = 1000000;
        logger.log(Level.INFO, "Sending {0} object messages for stress test", numObjects);
        personTest(1000000);
    }

    private void avroSchemaScenarioTest() {
        Vehicle vehicle = new Vehicle(2015, "Toyota", "Camry", 53000);
        assertThat(testContext.getVehicles().size()).isGreaterThan(0);
        assertThat(testContext.getVehicles().get(0)).isEqualTo(vehicle);
    }

    private void partitionScenarioTest() {
        for (int i = 0; i < 4; ++i) {
            String message = "test" + (i + 1);
            assertThat(testContext.getPartitions()[i]).isEqualTo(message);
        }
    }

    private void basicStringTest(int numMessages) {
        IntStream.range(0, numMessages).forEach(i -> template.send("string-in", ("test" + i).getBytes()));
        standardSleep();
        assertThat(testContext.getMessages().size()).isEqualTo(numMessages);
        assertMessages(numMessages);
    }

    private void personTest(int numPeople) {
        final String firstName = "Computer";
        final String lastName = "Programmer";
        IntStream.range(0, numPeople).forEach(i -> {
            Person person = new Person(firstName, lastName, i);
            template.send("person-in", person.toString().getBytes());
        });

        standardSleep();
        assertThat(testContext.getPeople().size()).isEqualTo(numPeople);
        assertPeople(numPeople, firstName, lastName);
    }

    private void standardSleep() {
        sleep(5);
    }

    private void assertMessages(int numMessages) {
        testContext.getMessages().sort(String::compareTo);
        for (int i = 0; i < numMessages; ++i) {
            assertThat(testContext.getMessages().get(i)).isEqualTo("test" + i);
        }
    }

    private void assertPeople(int numPeople, String firstName, String lastName) {
        testContext.getPeople().sort(Comparator.comparingInt(Person::getAge));
        Person person = new Person(firstName, lastName, 0);
        for (int i = 0; i < numPeople; ++i) {
            person.setAge(i);
            assertThat(testContext.getPeople().get(i)).isEqualTo(person);
        }
    }

    private Set<String> getKafkaTopics() {
        try {
            Properties properties = new Properties();
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
            AdminClient adminClient = AdminClient.create(properties);
            return adminClient.listTopics(new ListTopicsOptions().listInternal(true)).names().get();
        } catch (InterruptedException|ExecutionException e) {
            return new HashSet<>();
        }
    }

    private void runTest(Runnable method, String testCase) {
        try {
            method.run();
            logger.log(Level.INFO, "Test case with ID={0} passed", testCase);
        } catch (AssertionError e) {
            logger.log(Level.SEVERE, "{0} failed with assertion error: {1}", new Object[]{testCase, e.getMessage()});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "{0} failed with unexpected error: {1}", new Object[]{testCase, e.getMessage()});
        } finally {
            testContext.clear();
        }
    }

    private void configureFileHandler() throws IOException {
        FileHandler fileHandler = new FileHandler(logPath);
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        fileHandler.setFormatter(simpleFormatter);
        logger.addHandler(fileHandler);
    }
}
