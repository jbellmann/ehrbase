package org.ehrbase.modules.tenant;


import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ehrbase.api.tenant.Tenant;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantEntity implements Tenant {
  @Getter(AccessLevel.PACKAGE)
  private UUID id;
  @Getter
  private String tenantId;
  @Getter
  private String tenantName;

  public static TenantEntity of(String tenantId, String tenantName) {
    return new TenantEntity(UUID.randomUUID(), tenantId, tenantName);
  }
}
