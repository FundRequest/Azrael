package io.fundrequest.azrael.worker;

import io.fundrequest.azrael.worker.events.listener.FundRequestPlatformEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
public class AzraelWorkerApp {

    private static final Logger log = LoggerFactory.getLogger(AzraelWorkerApp.class);

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(AzraelWorkerApp.class);
        ConfigurableApplicationContext context = app.run(args);
        Environment env = context.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t"
                        + "Application '{}' is running! Access URLs:\n\t"
                        + "Local: \t\thttp://localhost:{}\n\t"
                        + "External: \thttp://{}:{}\n\t"
                        + "FR Contract Location: \t{}\n\t"
                        + "FR Token Location: \t{}\n\t"
                        + "FR TGE Location: \t{}\n\t"
                        + "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("io.fundrequest.contract.address"),
                env.getProperty("io.fundrequest.token.address"),
                env.getProperty("io.fundrequest.tge.address"),
                env.getActiveProfiles());
        context.getBean(FundRequestPlatformEventListener.class).subscribeToLive();

    }
}
