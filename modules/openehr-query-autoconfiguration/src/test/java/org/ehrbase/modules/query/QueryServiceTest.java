package org.ehrbase.modules.query;

import java.util.List;
import org.ehrbase.api.service.QueryService;
import org.ehrbase.modules.query.autoconfigure.QueryServiceConfiguration;
import org.ehrbase.response.ehrscape.QueryDefinitionResultDto;
import org.ehrbase.test.database.PostgresInitializer;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest(classes = {
    QueryServiceConfiguration.class,
    QueryServiceTest.CustomTestConfiguration.class,
    DataSourceAutoConfiguration.class,
    FlywayAutoConfiguration.class,
    JooqAutoConfiguration.class
})
@ContextConfiguration(initializers = {PostgresInitializer.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryServiceTest {

  @Autowired
  private QueryService queryService;

  @Test
  void startUpQueryService() {
    List<QueryDefinitionResultDto> results = queryService.retrieveStoredQueries(null);
  }

  @TestConfiguration
  static class CustomTestConfiguration {
    @Bean
    @Order(-10)
    public DefaultExecuteListenerProvider tenantExecuteListenerProvider() {
      return new DefaultExecuteListenerProvider(new TenantExecutionListener());
    }
  }
}
