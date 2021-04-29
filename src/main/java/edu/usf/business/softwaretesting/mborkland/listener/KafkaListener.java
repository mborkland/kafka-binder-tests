package edu.usf.business.softwaretesting.mborkland.listener;

import com.example.Vehicle;
import edu.usf.business.softwaretesting.mborkland.model.NotAPerson;
import edu.usf.business.softwaretesting.mborkland.model.Person;
import edu.usf.business.softwaretesting.mborkland.runner.test.TestContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
@AllArgsConstructor
public class KafkaListener {

    private final TestContext testContext;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Bean
    public Consumer<String> basicString() {
        return testContext::addMessage;
    }

    @Bean
    public Consumer<Person> person() {
        return testContext::addPerson;
    }

    @Bean
    public Consumer<List<String>> batch() {
        return testContext::addMessages;
    }

    @Bean
    public Consumer<NotAPerson> risk() {
        try {
            return testContext::addNotAPerson;
        } catch (MessageConversionException e) {
            return n -> testContext.setExceptionThrown(true);
        }
    }

    @Bean
    public Consumer<NotAPerson> notAPerson() {
        return testContext::addNotAPerson;
    }

    @Bean
    public Consumer<Object> deadLetter() {
        return testContext::addToQueue;
    }

    @Bean
    public Supplier<Vehicle> vehicleSupplier() {
        return () -> new Vehicle(2015, "Toyota", "Camry", 53000);
    }

    @Bean
    public Consumer<Vehicle> vehicle() {
        return testContext::addVehicle;
    }

    @Bean
    public Consumer<Message<String>> partition() {
        return m -> {
            Object header = m.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID);
            Assert.notNull(header, "Header should not be null");
            int index = Integer.parseInt(header.toString());
            testContext.addPartition(m.getPayload(), index);
        };
    }

    @Bean
    public Supplier<Message<?>> generatePartition() {
        return () -> {
            String[] strings = new String[]{"test1", "test2", "test3", "test4"};
            int random = RANDOM.nextInt(strings.length);
            String value = strings[random];
            return MessageBuilder.withPayload(value)
                    .setHeader("partitionKey", random)
                    .build();
        };
    }
}
