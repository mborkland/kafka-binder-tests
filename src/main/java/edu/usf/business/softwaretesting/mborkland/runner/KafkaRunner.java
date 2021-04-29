package edu.usf.business.softwaretesting.mborkland.runner;

import edu.usf.business.softwaretesting.mborkland.runner.test.FunctionalTestRunner;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

@Slf4j
@AllArgsConstructor
public class KafkaRunner implements CommandLineRunner {

    private final FunctionalTestRunner functionalTestRunner;

    @Override
    public void run(String... args) {
        functionalTestRunner.runAllTests();
        System.exit(0);
    }
}