package com.nowonbun.selenium;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
@EnableAdminServer
public class SeleniumApplication {
  private static final Logger log = LoggerFactory.getLogger(SeleniumApplication.class);
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(SeleniumApplication.class);
    app.addListeners(new ApplicationPidFileWriter());
    Environment env = app.run(args).getEnvironment();
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      log.warn("The host name could not be determined, using localhost as fallback");
    }
    int port = Integer.parseInt(env.getProperty("server.port", "8089"));
    String domainUrl =
        buildUrl(protocol, "localhost", port);
    String ipUrl = buildUrl(protocol, hostAddress, port);
    log.info(
        "\n----------------------------------------------------------\n\t"
            + "Application '{}' is running! Access URLs:\n\t"
            + "DOMAIN : \t{}\n\t"
            + "IP : \t\t{}\n\t"
            + "Profile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        domainUrl,
        ipUrl,
        env.getActiveProfiles());
  }
  private static String buildUrl(String scheme, String domain, int port) {
    return "%s://%s:%d".formatted(scheme, domain, port);
  }
}

