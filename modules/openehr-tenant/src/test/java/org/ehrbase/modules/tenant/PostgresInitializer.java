package org.ehrbase.modules.tenant;

import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class PostgresInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  static GenericContainer<?> postgres = new GenericContainer<>(
      DockerImageName.parse("ehrbase/ehrbase-postgres:latest"))
      .withEnv(Map.of(
          "POSTGRES_PASSWORD", "postgres",
          "POSTGRES_USER", "postgres",
          "EHRBASE_USER", "ehrbase",
          "EHRBASE_PASSWORD", "ehrbase"
      ))
      .withExposedPorts(5432);
      //.withClasspathResourceMapping("createdb.sql","/docker-entrypoint-initdb.d/", BindMode.READ_ONLY );

  public static Map<String, String> getProperties() {
    Startables.deepStart(Stream.of(postgres)).join();

    String url = "jdbc:postgresql://localhost:" + postgres.getMappedPort(5432) + "/ehrbase";
    log.info("URL TO POSTGRES : {}", url);
    return Map.of(
        "spring.datasource.url", url + "?currentSchema=ehr",
        "spring.flyway.url", url
    );
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var env = applicationContext.getEnvironment();
    env.getPropertySources().addFirst(new MapPropertySource(
        "testcontainers", (Map) getProperties()
    ));
  }
}
