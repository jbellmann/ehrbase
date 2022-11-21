package org.ehrbase.modules.query;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.ehrbase.api.definitions.QueryMode;
import org.ehrbase.api.exception.InvalidApiParameterException;
import org.ehrbase.api.service.QueryService;
import org.ehrbase.rest.ehrscape.responsedata.Action;
import org.ehrbase.rest.ehrscape.responsedata.QueryResponseData;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/rest/ecis/v1/query",
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@RequiredArgsConstructor
public class QueryController {
  private final QueryService queryService;

  @PostMapping
  public ResponseEntity<QueryResponseData> query(
      @RequestParam(value = "explain", defaultValue = "false") Boolean explain, @RequestBody() String content) {

    Map<String, String> kvPairs = extractQuery(new String(content.getBytes()));

    final String queryString;
    final QueryMode queryMode;
    if (kvPairs.containsKey(QueryMode.AQL.getCode())) {
      queryMode = QueryMode.AQL;
      queryString = kvPairs.get(QueryMode.AQL.getCode());
    } else if (kvPairs.containsKey(QueryMode.SQL.getCode())) {
      queryMode = QueryMode.SQL;
      queryString = kvPairs.get(QueryMode.SQL.getCode());
    } else {
      throw new InvalidApiParameterException("No query parameter supplied");
    }
    QueryResponseData responseData =
        new QueryResponseData(queryService.query(queryString, null, queryMode, explain, new HashMap<>()));

    responseData.setAction(Action.EXECUTE);
    return ResponseEntity.ok(responseData);
  }

  private static Map<String, String> extractQuery(String content) {
    Pattern patternKey = Pattern.compile("(?<=\\\")(.*?)(?=\")");
    Matcher matcherKey = patternKey.matcher(content);

    if (matcherKey.find()) {
      String type = matcherKey.group(1);
      String query = content.substring(content.indexOf(':') + 1, content.lastIndexOf('\"'));
      query = query.substring(query.indexOf('\"') + 1);
      Map<String, String> queryMap = new HashMap<>();
      queryMap.put(type.toLowerCase(), query);
      return queryMap;
    } else throw new IllegalArgumentException("Could not identified query type (sql or aql) in content:" + content);
  }
}
