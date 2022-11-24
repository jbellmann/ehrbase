package org.ehrbase.modules.query.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.ehrbase.api.service.QueryService;
import org.ehrbase.modules.query.OpenehrDefinitionQueryController;
import org.ehrbase.modules.query.OpenehrQueryController;
import org.ehrbase.modules.query.QueryController;
import org.ehrbase.modules.query.QueryRepository;
import org.ehrbase.modules.query.StandardQueryService;
import org.ehrbase.rest.openehr.RequestAwareAuditResultMapHolder;
import org.ehrbase.validation.terminology.ExternalTerminologyValidation;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class QueryServiceConfiguration {

  @Bean
  QueryService queryService(ExternalTerminologyValidation validation, DSLContext dslContext) {
    log.debug("Create QueryService ...");
    StandardQueryService svc = new StandardQueryService(
        validation,
        new QueryRepository(dslContext)
    );
    return svc;
  }

  @Bean
  QueryController queryController(QueryService queryService) {
    log.debug("Create QueryController ...");
    return new QueryController(queryService);
  }

  @Bean
  OpenehrDefinitionQueryController openehrDefinitionQueryController(QueryService queryService) {
    log.debug("Create OpenehrDefinitionQueryController ...");
    return new OpenehrDefinitionQueryController(queryService);
  }

  @Bean
  OpenehrQueryController openehrQueryController(QueryService queryService, RequestAwareAuditResultMapHolder requestAwareAuditResultMapHolder) {
    log.debug("Create OpenehrQueryController ...");
    return new OpenehrQueryController(queryService, requestAwareAuditResultMapHolder);
  }

  @Bean
  RequestAwareAuditResultMapHolder requestAwareAuditResultMapHolder() {
    log.debug("Create RequestAwareAuditResultMapper ...");
    return new RequestAwareAuditResultMapHolder();
  }

  @Bean
  @ConditionalOnMissingBean(ExternalTerminologyValidation.class)
  ExternalTerminologyValidation externalTerminologyValidation() {
    log.debug("Create ExternalTerminologyValidation ...");
    return new NoOpExternalTerminologyValidation();
  }
}
