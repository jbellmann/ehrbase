package org.ehrbase.modules.tenant;

import static org.ehrbase.jooq.pg.tables.Tenant.TENANT;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ehrbase.api.exception.ObjectNotFoundException;
import org.ehrbase.api.tenant.Tenant;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class TenantRepository {
  private final DSLContext db;

  /**
   *
   * @param tenant
   * @return UUID for generated object
   * @throws 'org.springframework.dao.DuplicateKeyException' in case a tenant with same 'tenantId'
   * already exists in DB.
   */
  public UUID insert(Tenant tenant) {
    final UUID uuid = UUID.randomUUID();
    db.insertInto(TENANT)
        .set(TENANT.ID, uuid)
        .set(TENANT.TENANT_ID, tenant.getTenantId())
        .set(TENANT.TENANT_NAME, tenant.getTenantName())
        .execute();
    return uuid;
  }

  public Optional<Tenant> getById(String tenantId) {
    return Optional.ofNullable(
        db.selectFrom(TENANT)
            .where(TENANT.TENANT_ID.eq(tenantId))
            .fetchOneInto(TenantEntity.class)
    );
  }

  public List<Tenant> getAll() {
    return db.selectFrom(TENANT).fetchInto(TenantEntity.class);
  }

  /**
   *
   * @param tenant
   * @return updated tenant
   * @throws ObjectNotFoundException in case no {@link Tenant} with {@link Tenant#getTenantId()}
   * exist in DB.
   */
  public Tenant update(Tenant tenant) {
    int rowsAffected = db.update(TENANT)
        .set(TENANT.TENANT_NAME, tenant.getTenantName())
        .where(TENANT.TENANT_ID.eq(tenant.getTenantId()))
        .execute();

    if(rowsAffected != 1) {
      // do we have a different exception on multiple rows affected?
      throw new ObjectNotFoundException(Tenant.class.getSimpleName(), String.format("No tenant or single tenant found with 'tenantId' = '%s'", tenant.getTenantId()));
    }
    return tenant;
  }
}
