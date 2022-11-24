package org.ehrbase.modules.tenant.config;

import lombok.extern.slf4j.Slf4j;
import org.ehrbase.modules.tenant.TenantRepository;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class TenantRepositoryConfiguration {

  @Bean
  TenantRepository tenantRepository(DSLContext db) {
    log.info("Create TenantRepository ...");
    return new TenantRepository(db);
  }
}
