package org.ehrbase.modules.query;

import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.api.tenant.TenantAuthentication;
import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;

@Slf4j
public class TenantExecutionListener extends DefaultExecuteListener {

  @Override
  public void executeStart(ExecuteContext executeContext) {
    log.debug("SET CURRENT TENANT : {}", createSqlStatement());
    try {
      executeContext
          .connection()
          .createStatement()
          .execute(createSqlStatement());
    } catch (SQLException e) {
      log.error("Error setting tenant : {}", getTenantId());
      throw new RuntimeException("Unable to set current Tenant", e);
    }
  }

  protected String getTenantId() {
    return TenantAuthentication.DEFAULT_TENANT_ID;
  }

  protected String createSqlStatement() {
    return String.format("SET ehrbase.current_tenant = '%s'", getTenantId());
  }
}
