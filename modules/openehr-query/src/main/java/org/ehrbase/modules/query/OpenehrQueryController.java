package org.ehrbase.modules.query;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.api.definitions.QueryMode;
import org.ehrbase.api.exception.InvalidApiParameterException;
import org.ehrbase.api.exception.ObjectNotFoundException;
import org.ehrbase.api.service.QueryService;
import org.ehrbase.modules.query.specification.QueryApiSpecification;
import org.ehrbase.response.ehrscape.QueryDefinitionResultDto;
import org.ehrbase.response.openehr.QueryResponseData;
import org.ehrbase.rest.openehr.RequestAwareAuditResultMapHolder;
import org.ehrbase.rest.openehr.audit.OpenEhrAuditInterceptor;
import org.ehrbase.rest.openehr.audit.QueryAuditInterceptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "${openehr-api.context-path:/rest/openehr}/v1/query")
@RequiredArgsConstructor
public class OpenehrQueryController implements QueryApiSpecification {
  private static final String EHR_ID_VALUE = "ehr_id/value";
  private static final String LATEST = "LATEST";
  private static final String QUERY_PARAMETERS = "query_parameters";
  private final QueryService queryService;
  private final RequestAwareAuditResultMapHolder auditResultMapHolder;




  /**
   * {@inheritDoc}
   */
  @Override
  @GetMapping(path = "/aql")
  @PostAuthorize("checkAbacPostQuery(@requestAwareAuditResultMapHolder.getAuditResultMap())")
  public ResponseEntity<QueryResponseData> executeAdHocQuery(
      @RequestParam(name = "q") String query,
      @RequestParam(name = "offset", required = false) Integer offset,
      @RequestParam(name = "fetch", required = false) Integer fetch,
      @RequestParam(name = "query_parameters", required = false) Map<String, Object> queryParameters,
      @RequestHeader(name = ACCEPT, required = false) String accept,
      HttpServletRequest request) {

    // deal with offset and fetch
    if (fetch != null) {
      query = withFetch(query, fetch);
    }

    if (offset != null) {
      query = withOffset(query, offset);
    }

    // Enriches request attributes with aql for later audit processing
    request.setAttribute(QueryAuditInterceptor.QUERY_ATTRIBUTE, query);

    var body = executeQuery(query, queryParameters, request);

    if (!CollectionUtils.isEmpty(body.getRows())) {
      return ResponseEntity.ok(body);
    } else {
      return ResponseEntity.noContent().build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PostMapping(path = "/aql")
  @PostAuthorize("checkAbacPostQuery(@requestAwareAuditResultMapHolder.getAuditResultMap())")
  @SuppressWarnings("unchecked")
  public ResponseEntity<QueryResponseData> executeAdHocQuery(
      @RequestBody Map<String, Object> queryRequest,
      @RequestHeader(name = ACCEPT, required = false) String accept,
      @RequestHeader(name = CONTENT_TYPE) String contentType,
      HttpServletRequest request) {

    log.debug("Got following input: {}", queryRequest);

    String aql = (String) queryRequest.get("q");
    if (aql == null) {
      throw new InvalidApiParameterException("No aql query provided");
    }

    aql = withOffsetLimit(aql, queryRequest);
    // Enriches request attributes with aql for later audit processing
    request.setAttribute(QueryAuditInterceptor.QUERY_ATTRIBUTE, aql);

    Map<String, Object> parameters = (Map<String, Object>) queryRequest.get(QUERY_PARAMETERS);

    var body = executeQuery(aql, parameters, request);
    return ResponseEntity.ok(body);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @GetMapping(path = {"/{qualified_query_name}", "/{qualified_query_name}/{version}"})
  @PostAuthorize("checkAbacPostQuery(@requestAwareAuditResultMapHolder.getAuditResultMap())")
  public ResponseEntity<QueryResponseData> executeStoredQuery(
      @PathVariable(name = "qualified_query_name") String qualifiedQueryName,
      @PathVariable(name = "version", required = false) String version,
      @RequestParam(name = "offset", required = false) Integer offset,
      @RequestParam(name = "fetch", required = false) Integer fetch,
      @RequestParam(name = "query_parameters", required = false) Map<String, Object> queryParameter,
      @RequestHeader(name = ACCEPT, required = false) String accept,
      HttpServletRequest request) {

    log.trace(
        "getStoredQuery not implemented but got following input: {} - {} - {} - {} - {}",
        qualifiedQueryName,
        version,
        offset,
        fetch,
        queryParameter);
    // Enriches request attributes with query name for later audit processing
    request.setAttribute(QueryAuditInterceptor.QUERY_ID_ATTRIBUTE, qualifiedQueryName);

    // retrieve the stored query for execution
    QueryDefinitionResultDto queryDefinitionResultDto =
        queryService.retrieveStoredQuery(qualifiedQueryName, version != null ? version : LATEST);

    String query = queryDefinitionResultDto.getQueryText();

    // Enriches request attributes with aql for later audit processing
    request.setAttribute(QueryAuditInterceptor.QUERY_ATTRIBUTE, query);

    if (fetch != null) {
      // append LIMIT clause to aql
      query = withFetch(query, fetch);
    }

    if (offset != null) {
      // append OFFSET clause to aql
      query = withOffset(query, offset);
    }

    QueryResponseData queryResponseData = invoke(query, queryParameter, request);
    queryResponseData.setName(
        queryDefinitionResultDto.getQualifiedName() + "/" + queryDefinitionResultDto.getVersion());
    return ResponseEntity.ok(queryResponseData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @PostMapping(path = {"/{qualified_query_name}", "/{qualified_query_name}/{version}"})
  @PostAuthorize("checkAbacPostQuery(@requestAwareAuditResultMapHolder.getAuditResultMap())")
  @SuppressWarnings("unchecked")
  public ResponseEntity<QueryResponseData> executeStoredQuery(
      @PathVariable(name = "qualified_query_name") String qualifiedQueryName,
      @PathVariable(name = "version", required = false) String version,
      @RequestHeader(name = ACCEPT, required = false) String accept,
      @RequestHeader(name = CONTENT_TYPE) String contentType,
      @RequestBody(required = false) Map<String, Object> queryRequest,
      HttpServletRequest request) {

    log.trace("postStoredQuery with the following input: {}, {}, {}", qualifiedQueryName, version, queryRequest);

    // retrieve the stored query for execution
    request.setAttribute(QueryAuditInterceptor.QUERY_ID_ATTRIBUTE, qualifiedQueryName);

    QueryDefinitionResultDto queryDefinitionResultDto =
        queryService.retrieveStoredQuery(qualifiedQueryName, version != null ? version : LATEST);

    String query = queryDefinitionResultDto.getQueryText();

    if (query == null) {
      var message = MessageFormat.format(
          "Could not retrieve AQL {0}/{1}", qualifiedQueryName, version != null ? version : LATEST);
      throw new ObjectNotFoundException("AQL", message);
    }

    // Enriches request attributes with aql for later audit processing
    request.setAttribute(QueryAuditInterceptor.QUERY_ATTRIBUTE, query);

    // retrieve the parameter from body
    // get the query and parameters if any
    Map<String, Object> queryParameter = null;

    if (queryRequest != null && !queryRequest.isEmpty()) {
      queryParameter = (Map<String, Object>) queryRequest.get(QUERY_PARAMETERS);

      query = withOffsetLimit(query, queryRequest);
    }
    QueryResponseData queryResponseData = invoke(query, queryParameter, request);

    queryResponseData.setName(
        queryDefinitionResultDto.getQualifiedName() + "/" + queryDefinitionResultDto.getVersion());
    return ResponseEntity.ok(queryResponseData);
  }

  private QueryResponseData executeQuery(String aql, Map<String, Object> parameters, HttpServletRequest request) {
    QueryResponseData queryResponseData;

    Map<String, Set<Object>> auditResultMap = auditResultMapHolder.getAuditResultMap();

    // get the query and pass it to the service
    queryResponseData =
        new QueryResponseData(queryService.query(aql, parameters, QueryMode.AQL, false, auditResultMap));

    // Enriches request attributes with EhrId(s) for later audit processing
    request.setAttribute(OpenEhrAuditInterceptor.EHR_ID_ATTRIBUTE, auditResultMap.get(EHR_ID_VALUE));

    return queryResponseData;
  }

  private String withFetch(String query, String value) {
    return withFetch(query, double2int(value));
  }

  private String withFetch(String query, Integer value) {
    return orderedLimitOffset(query, "LIMIT", value);
  }

  private String orderedLimitOffset(String query, String keyword, Integer value) {
    String queryFormatted;

    if (query.replace(" ", "").toUpperCase().contains("ORDERBY")) {
      // insert LIMIT before ORDER BY clause!
      String[] strings = query.split("(?i)ORDER");
      // assemble
      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append(strings[0]);
      queryBuilder.append(keyword.toUpperCase());
      queryBuilder.append(" ");
      queryBuilder.append(value);
      queryBuilder.append(" ORDER");
      queryBuilder.append(strings[1]);
      queryFormatted = queryBuilder.toString();
    } else {
      queryFormatted = query + " " + keyword + " " + value;
    }

    return queryFormatted;
  }

  private String withOffset(String query, String value) {
    return withOffset(query, double2int(value));
  }

  private String withOffset(String query, Integer value) {
    return orderedLimitOffset(query, "OFFSET", value);
  }

  private Integer double2int(String value) {
    return (Double.valueOf(value)).intValue();
  }

  private QueryResponseData invoke(String query, Map<String, Object> queryParameter, HttpServletRequest request) {
    QueryResponseData queryResponseData;

    Map<String, Set<Object>> auditResultMap = auditResultMapHolder.getAuditResultMap();

    if (queryParameter != null && !queryParameter.isEmpty()) {
      Map<String, Object> parameters = new HashMap<>(queryParameter);
      queryResponseData =
          new QueryResponseData(queryService.query(query, parameters, QueryMode.AQL, false, auditResultMap));
    } else {
      queryResponseData =
          new QueryResponseData(queryService.query(query, null, QueryMode.AQL, false, auditResultMap));
    }

    // Enriches request attributes with EhrId(s) for later audit processing
    request.setAttribute(OpenEhrAuditInterceptor.EHR_ID_ATTRIBUTE, auditResultMap.get(EHR_ID_VALUE));

    return queryResponseData;
  }

  String withOffsetLimit(String query, Map<String, Object> mapped) {
    if (mapped.containsKey("fetch")) {
      // append LIMIT clause to aql
      query = withFetch(query, mapped.get("fetch").toString());
    }

    if (mapped.containsKey("offset")) {
      // append OFFSET clause to aql
      query = withOffset(query, mapped.get("offset").toString());
    }

    return query;
  }
}
