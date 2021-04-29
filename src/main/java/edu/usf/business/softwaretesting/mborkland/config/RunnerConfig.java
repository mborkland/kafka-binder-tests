package edu.usf.business.softwaretesting.mborkland.config;

import edu.usf.business.softwaretesting.mborkland.runner.KafkaRunner;
import edu.usf.business.softwaretesting.mborkland.runner.test.FunctionalTestRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RunnerConfig {

    @Bean
    public CommandLineRunner commandLineRunner(FunctionalTestRunner functionalTestRunner) {
        return new KafkaRunner(functionalTestRunner);
    }
}
