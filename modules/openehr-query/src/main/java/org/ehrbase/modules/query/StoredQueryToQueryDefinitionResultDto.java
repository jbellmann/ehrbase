package org.ehrbase.modules.query;

import java.time.ZoneId;
import org.ehrbase.jooq.pg.tables.records.StoredQueryRecord;
import org.ehrbase.response.ehrscape.QueryDefinitionResultDto;
import org.jooq.RecordMapper;

class StoredQueryToQueryDefinitionResultDto implements RecordMapper<StoredQueryRecord, QueryDefinitionResultDto> {

  public static final String DOUBLE_COLON = "::";

  @Override
  public QueryDefinitionResultDto map(StoredQueryRecord storedQueryRecord) {
    QueryDefinitionResultDto dto = new QueryDefinitionResultDto();
    dto.setQueryText(storedQueryRecord.getQueryText());
    dto.setType(storedQueryRecord.getType());
    dto.setQualifiedName(storedQueryRecord.getReverseDomainName() + DOUBLE_COLON + storedQueryRecord.getSemanticId());
    dto.setVersion(storedQueryRecord.getSemver());
    dto.setSaved(storedQueryRecord.getCreationDate().toInstant().atZone(ZoneId.systemDefault()));
    return dto;
  }
}
