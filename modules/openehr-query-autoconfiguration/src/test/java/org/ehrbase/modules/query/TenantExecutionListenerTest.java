package org.ehrbase.modules.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TenantExecutionListenerTest {

  @Test
  void testListener() {
    TenantExecutionListener l = new TenantExecutionListener();
    String tenantId = l.getTenantId();
    String sqlStatement = l.createSqlStatement();
    assertNotNull(tenantId);
    assertNotNull(sqlStatement);
    log.info("TenantId : {}", tenantId);
    log.info("Statement : {}", sqlStatement);
  }
}
