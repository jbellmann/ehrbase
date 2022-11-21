package org.ehrbase.modules.query;

import static org.ehrbase.jooq.pg.Tables.STORED_QUERY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.dao.access.util.StoredQueryQualifiedName;
import org.ehrbase.response.ehrscape.QueryDefinitionResultDto;
import org.ehrbase.response.ehrscape.QueryResultDto;
import org.ehrbase.response.ehrscape.query.ResultHolder;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Record;

@Slf4j
@RequiredArgsConstructor
public class QueryRepository {
  public static final String RESULT_SET = "resultSet";
  public static final String LATEST = "LATEST";
  private final DSLContext db;

  private final StoredQueryToQueryDefinitionResultDto mapper = new StoredQueryToQueryDefinitionResultDto();

  QueryResultDto query(String queryString) {
    Result<Record> r = db.fetch(queryString); // I hope someone verifies here what kind of sql is incoming
    Map<String, Object> resultMap = new HashMap<>();
    List<Map<String, Object>> resultList = new ArrayList<>();
    for (Record current : r) {
      Map<String, Object> fieldMap = new HashMap<>();
      for (Field<?> field : r.fields()) {
        fieldMap.put(field.getName(), current.getValue(field));
      }
      resultList.add(fieldMap);
    }

    resultMap.put(RESULT_SET, resultList);

    QueryResultDto dto = new QueryResultDto();
    dto.setExecutedAQL(queryString);
    dto.setResultSet((List<ResultHolder>) resultMap.get(RESULT_SET)); // directly setting the 'resultList' does not compile
    // dto.setExplain((List<List<String>>) result.get("explain")); // where should this come from

    return dto;
  }

  List<QueryDefinitionResultDto> retrieveStoredQueries() {
    return db.selectFrom(STORED_QUERY)
        .orderBy(STORED_QUERY.SEMVER.desc())
        .fetch(mapper);
  }

  List<QueryDefinitionResultDto> retrieveStoredQueries(String qualifiedName) {
    StoredQueryQualifiedName storedQueryQualifiedName = new StoredQueryQualifiedName(qualifiedName);
    return db.selectFrom(STORED_QUERY)
        .where(STORED_QUERY
            .REVERSE_DOMAIN_NAME
            .eq(storedQueryQualifiedName.reverseDomainName())
            .and(STORED_QUERY.SEMANTIC_ID.eq(storedQueryQualifiedName.semanticId())))
        .orderBy(STORED_QUERY.SEMVER.desc())
        .fetch(mapper);
  }

  QueryDefinitionResultDto retrieveStoredQuery(String qualifiedName, String version) {
    String queryQualifiedName = qualifiedName + ((version != null && !version.isEmpty()) ? "/" + version : "");
    List<QueryDefinitionResultDto> resultList = this.getByFullyQualifiedName(new StoredQueryQualifiedName(queryQualifiedName));
    if(resultList.isEmpty()) {
      log.warn("Could not retrieve stored query for qualified name:" + qualifiedName);
      // TODO: is there a better exception type like 'ReourceNotFound'...
      throw new IllegalArgumentException("Could not retrieve stored query for qualified name:" + qualifiedName);
    } else {
      return resultList.stream().findFirst().get();
    }
  }

  QueryDefinitionResultDto createStoredQuery(String qualifiedName, String version, String queryString) {
    String queryQualifiedName = qualifiedName + ((version != null && !version.isEmpty()) ? "/" + version : "");
    new StoredQueryQualifiedName(queryQualifiedName);


    // create one

    return new QueryDefinitionResultDto();
  }

  QueryDefinitionResultDto updateStoredQuery(String qualifiedName, String version, String queryString) {
    String queryQualifiedName = qualifiedName + ((version != null && !version.isEmpty()) ? "/" + version : "");
    new StoredQueryQualifiedName(queryQualifiedName);

    // update existing one

    return new QueryDefinitionResultDto();
  }

  public QueryDefinitionResultDto deleteStoredQuery(String qualifiedName, String version) {
    String queryQualifiedName = qualifiedName + ((version != null && !version.isEmpty()) ? "/" + version : "");
    new StoredQueryQualifiedName(queryQualifiedName);

    // delete existing one

    return new QueryDefinitionResultDto();
  }

  protected List<QueryDefinitionResultDto> getByFullyQualifiedName(StoredQueryQualifiedName storedQueryQualifiedName) {
    if(LATEST.equalsIgnoreCase(storedQueryQualifiedName.semVer())) {
      return db.selectFrom(STORED_QUERY)
          .where(STORED_QUERY.REVERSE_DOMAIN_NAME
              .eq(storedQueryQualifiedName.reverseDomainName())
              .and(STORED_QUERY.SEMANTIC_ID.eq(storedQueryQualifiedName.semanticId()))
              .and(STORED_QUERY.SEMVER.like(storedQueryQualifiedName.semVer() + "%")))
          .orderBy(STORED_QUERY.SEMVER.desc())
          .limit(1)
          .fetch(mapper);
    } else {
      return db.selectFrom(STORED_QUERY)
          .where(STORED_QUERY.REVERSE_DOMAIN_NAME
              .eq(storedQueryQualifiedName.reverseDomainName())
              .and(STORED_QUERY.SEMANTIC_ID.eq(storedQueryQualifiedName.semanticId())))
          .orderBy(STORED_QUERY.SEMVER.desc())
          .limit(1)
          .fetch(mapper);
    }
  }
}
