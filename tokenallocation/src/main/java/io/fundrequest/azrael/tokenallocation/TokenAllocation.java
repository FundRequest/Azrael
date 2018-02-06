package io.fundrequest.azrael.tokenallocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
public class TokenAllocation {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(TokenAllocation.class);
        Environment env = app.run(args).getEnvironment();
        log.info("\n----------------------------------------------------------\n\t"
                        + "Application '{}' is running! Access URLs:\n\t"
                        + "Local: \t\thttp://localhost:{}\n\t"
                        + "External: \thttp://{}:{}\n\t"
                        + "FR Token Location: \t{}\n\t"
                        + "FR TGE Location: \t{}\n\t"
                        + "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("io.fundrequest.token.address"),
                env.getProperty("io.fundrequest.tge.address"),
                env.getActiveProfiles());
    }

}
