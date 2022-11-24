package de.vitagroup.ehrbase.community;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class QueryControllerIT {

  @Test
  void getMappings() {
    RestTemplate rest = new RestTemplate();
    ResponseEntity<String> response = rest.getForEntity("http://localhost:9999/actuator/mappings", String.class);
    log.info(response.getBody());
  }

  @Test
  @Disabled
  void query() {
    RestTemplate rest = new RestTemplate();
    RequestEntity<String> req = RequestEntity
        .post("http://localhost:9080/rest/ecis/v1/query")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body("some text that should fail");

    ResponseEntity<String> response = rest.exchange(req, String.class);
    log.info(response.getBody());
  }
}
