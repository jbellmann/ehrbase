package org.ehrbase.modules.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.ehrbase.api.exception.ObjectNotFoundException;
import org.ehrbase.api.tenant.Tenant;
import org.ehrbase.modules.tenant.config.TenantRepositoryConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest(classes = {
    TenantRepositoryConfiguration.class,
    DataSourceAutoConfiguration.class,
    FlywayAutoConfiguration.class,
    JooqAutoConfiguration.class
})
@ContextConfiguration(initializers = {PostgresInitializer.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TenantRepositoryTest {

  public static final String MY_TEST_TENANT = "myTestTenant";
  public static final String ANOTHER_TENANT_NAME = "anotherTenantName";
  @Autowired
  private TenantRepository tenantRepository;

  @Test
  void startUpQueryService() {
    UUID uuid = tenantRepository.insert(TenantEntity.of(MY_TEST_TENANT, "myTestTenantName"));
    Optional<Tenant> optTenant = tenantRepository.getById(MY_TEST_TENANT);
    assertTrue(optTenant.isPresent());
    Tenant tenant = optTenant.get();
    TenantEntity entity = (TenantEntity) tenant;
    assertEquals(uuid, entity.getId());
    tenantRepository.update(TenantEntity.of(MY_TEST_TENANT, ANOTHER_TENANT_NAME));
    optTenant = tenantRepository.getById(MY_TEST_TENANT);
    assertTrue(optTenant.isPresent());
    assertEquals(ANOTHER_TENANT_NAME, optTenant.get().getTenantName());

    assertThrows(ObjectNotFoundException.class, () -> {
      tenantRepository.update(TenantEntity.of("unknownTenant", ANOTHER_TENANT_NAME));
    });

    assertThrows(DuplicateKeyException.class, () -> {
      tenantRepository.insert(TenantEntity.of(MY_TEST_TENANT, "myTestTenantName"));
    });
  }

}
