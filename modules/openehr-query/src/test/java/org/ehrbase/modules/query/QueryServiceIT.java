package org.ehrbase.modules.query;

import org.ehrbase.api.service.QueryService;
import org.ehrbase.validation.terminology.ExternalTerminologyValidation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

/**
@SpringBootTest(classes = {
    QueryServiceConfiguration.class,
    QueryServiceIT.QueryServiceTestConfiguration.class,
    DataSourceAutoConfiguration.class,
    JooqAutoConfiguration.class
})
// @ContextConfiguration(initializers = {PostgresInitializer.class})
 */
@Disabled
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QueryServiceIT {

  @Autowired
  private QueryService queryService;

  @Test
  void startUpQueryService() {

    // List<QueryDefinitionResultDto> results = queryService.retrieveStoredQueries(null);
  }

  @TestConfiguration
  static class QueryServiceTestConfiguration {

    @Bean
    ExternalTerminologyValidation noOp() {
      return new NoOpExternalTerminologyValidation();
    }
  }
}
