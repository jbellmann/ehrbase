package org.ehrbase.modules.query;

import org.ehrbase.test.database.PostgresInitializer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@JooqTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresInitializer.class})
public class QueryRepositoryTest {

  @Autowired
  private QueryRepository queryRepository;

  @Test
  void testSomething() {
    // I know, that's already given by @Autowired annotation
    // this is all just for demonstration purposes
    Assertions.assertNotNull(queryRepository);
  }

  @TestConfiguration
  static class RepositoryConfiguration {

    @Bean
    QueryRepository queryRepository(DSLContext dslContext) {
      return new QueryRepository(dslContext);
    }
  }
}
