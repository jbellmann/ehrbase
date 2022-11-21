package org.ehrbase.modules.query;

import static org.springframework.http.HttpHeaders.ACCEPT;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.api.service.QueryService;
import org.ehrbase.modules.query.specification.DefinitionQueryApiSpecification;
import org.ehrbase.response.openehr.ErrorBodyPayload;
import org.ehrbase.response.openehr.QueryDefinitionListResponseData;
import org.ehrbase.response.openehr.QueryDefinitionResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(
    path = "${openehr-api.context-path:/rest/openehr}/v1/definition/query",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@RequiredArgsConstructor
public class OpenehrDefinitionQueryController implements DefinitionQueryApiSpecification {

  private final QueryService queryService;

  // ----- DEFINITION: Manage Stored Query, From definition package
  // https://openehr.github.io/specifications-ITS-REST/definitions.html#definitions-stored-query

  /**
   * Get a stored query
   *
   * @param accept
   * @param qualifiedQueryName
   * @return
   */
  @RequestMapping(
      value = {"/{qualified_query_name}", ""},
      method = RequestMethod.GET)
  @Override
  public ResponseEntity<QueryDefinitionListResponseData> getStoredQueryList(
      @RequestHeader(value = ACCEPT, required = false) String accept,
      @PathVariable(value = "qualified_query_name", required = false) String qualifiedQueryName) {

    log.debug("getStoredQueryList invoked with the following input: " + qualifiedQueryName);

    QueryDefinitionListResponseData responseData =
        new QueryDefinitionListResponseData(queryService.retrieveStoredQueries(qualifiedQueryName));
    return ResponseEntity.ok(responseData);
  }

  @RequestMapping(
      value = {"/{qualified_query_name}/{version}"},
      method = RequestMethod.GET) //
  @Override
  public ResponseEntity<QueryDefinitionResponseData> getStoredQueryVersion(
      @RequestHeader(value = ACCEPT, required = false) String accept,
      @PathVariable(value = "qualified_query_name") String qualifiedQueryName,
      @PathVariable(value = "version") Optional<String> version) {

    log.debug("getStoredQueryVersion invoked with the following input: " + qualifiedQueryName + ", version:"
        + version);

    QueryDefinitionResponseData queryDefinitionResponseData = new QueryDefinitionResponseData(
        queryService.retrieveStoredQuery(qualifiedQueryName, version.isPresent() ? version.get() : null));

    return ResponseEntity.ok(queryDefinitionResponseData);
  }

  @RequestMapping(
      value = {"/{qualified_query_name}/{version}{?type}", "/{qualified_query_name}{?type}"},
      method = RequestMethod.PUT)
  @Override
  public ResponseEntity<QueryDefinitionResponseData> putStoreQuery(
      @RequestHeader(value = ACCEPT, required = false) String accept,
      @PathVariable(value = "qualified_query_name") String qualifiedQueryName,
      @PathVariable(value = "version") Optional<String> version,
      @RequestParam(value = "type", required = false, defaultValue = "AQL") String type,
      @RequestBody String queryPayload) {

    log.debug("putStoreQuery invoked with the following input: " + qualifiedQueryName + ", version:" + version
        + ", query:" + queryPayload + ", type=" + type);

    // use the payload from adhoc POST:
    // get the query and parameters if any
    Gson gson = new GsonBuilder().create();

    Map<String, Object> mapped = gson.fromJson(queryPayload, Map.class);
    String aql = (String) mapped.get("q");

    if (aql == null || aql.isEmpty())
      return new ResponseEntity(
          new ErrorBodyPayload("Invalid query", "no aql query provided in payload").toString(),
          HttpStatus.BAD_REQUEST);

    QueryDefinitionResponseData queryDefinitionResponseData = new QueryDefinitionResponseData(
        queryService.createStoredQuery(qualifiedQueryName, version.orElse(null), aql));

    return ResponseEntity.ok(queryDefinitionResponseData);
  }

  @RequestMapping(
      value = {"/{qualified_query_name}/{version}"},
      method = RequestMethod.DELETE)
  @Override
  public ResponseEntity<QueryDefinitionResponseData> deleteStoredQuery(
      @RequestHeader(value = ACCEPT, required = false) String accept,
      @PathVariable(value = "qualified_query_name") String qualifiedQueryName,
      @PathVariable(value = "version") String version) {

    log.debug("deleteStoredQuery for the following input: {} , version: {}", qualifiedQueryName, version);

    QueryDefinitionResponseData queryDefinitionResponseData =
        new QueryDefinitionResponseData(queryService.deleteStoredQuery(qualifiedQueryName, version));

    return ResponseEntity.ok(queryDefinitionResponseData);
  }
}
