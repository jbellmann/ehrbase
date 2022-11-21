package org.ehrbase.modules.query.autoconfigure;

import org.ehrbase.api.service.QueryService;
import org.ehrbase.modules.query.OpenehrDefinitionQueryController;
import org.ehrbase.modules.query.OpenehrQueryController;
import org.ehrbase.modules.query.QueryController;
import org.ehrbase.modules.query.QueryRepository;
import org.ehrbase.modules.query.StandardQueryService;
import org.ehrbase.rest.openehr.RequestAwareAuditResultMapHolder;
import org.ehrbase.validation.terminology.ExternalTerminologyValidation;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StandardQueryService.class)
public class QueryServiceConfiguration {

  @Bean
  QueryService queryService(ExternalTerminologyValidation validation, DSLContext dslContext) {
    StandardQueryService svc = new StandardQueryService(
        validation,
        new QueryRepository(dslContext)
    );
    return svc;
  }

  @Bean
  QueryController queryController(QueryService queryService) {
    return new QueryController(queryService);
  }

  @Bean
  OpenehrDefinitionQueryController openehrDefinitionQueryController(QueryService queryService) {
    return new OpenehrDefinitionQueryController(queryService);
  }

  @Bean
  OpenehrQueryController openehrQueryController(QueryService queryService, RequestAwareAuditResultMapHolder requestAwareAuditResultMapHolder) {
    return new OpenehrQueryController(queryService, requestAwareAuditResultMapHolder);
  }

  @Bean
  RequestAwareAuditResultMapHolder requestAwareAuditResultMapHolder() {
    return new RequestAwareAuditResultMapHolder();
  }

  @Bean
  @ConditionalOnMissingBean(ExternalTerminologyValidation.class)
  ExternalTerminologyValidation externalTerminologyValidation() {
    return new NoOpExternalTerminologyValidation();
  }
}
