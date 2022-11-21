package org.ehrbase.modules.query;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.ehrbase.aql.sql.AqlResult;
import org.ehrbase.response.ehrscape.QueryResultDto;
import org.ehrbase.response.ehrscape.StructuredString;
import org.ehrbase.response.ehrscape.StructuredStringFormat;
import org.ehrbase.response.ehrscape.query.ResultHolder;
import org.jooq.Field;

@UtilityClass
class AqlResultMapper {

  static QueryResultDto formatResult(AqlResult aqlResult, String queryString, boolean explain) {
    QueryResultDto dto = new QueryResultDto();
    dto.setExecutedAQL(queryString);
    dto.setVariables(aqlResult.getVariables());

    List<ResultHolder> resultList = new ArrayList<>();
    for (org.jooq.Record record : aqlResult.getRecords()) {
      ResultHolder fieldMap = new ResultHolder();
      for (Field field : record.fields()) {
        // process non-hidden variables
        if (aqlResult.variablesContains(field.getName())) {
          // check whether to use field name or alias
          if (record.getValue(field) instanceof JsonElement) {
            fieldMap.putResult(
                field.getName(),
                new StructuredString((record.getValue(field)).toString(), StructuredStringFormat.JSON));
          } else fieldMap.putResult(field.getName(), record.getValue(field));
        }
      }

      resultList.add(fieldMap);
    }

    dto.setResultSet(resultList);
    if (explain) {
      dto.setExplain(aqlResult.getExplain());
    }

    return dto;
  }

}
