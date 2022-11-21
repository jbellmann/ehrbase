package org.ehrbase.modules.query;

import com.nedap.archie.rm.datavalues.DvCodedText;
import java.util.List;
import org.ehrbase.functional.Try;
import org.ehrbase.validation.ConstraintViolationException;
import org.ehrbase.validation.terminology.ExternalTerminologyValidation;
import org.ehrbase.validation.terminology.TerminologyParam;

public class NoOpExternalTerminologyValidation implements ExternalTerminologyValidation {

  @Override
  public boolean supports(TerminologyParam terminologyParam) {
    return false;
  }

  @Override
  public Try<Boolean, ConstraintViolationException> validate(TerminologyParam terminologyParam) {
    return Try.success(Boolean.FALSE);
  }

  @Override
  public List<DvCodedText> expand(TerminologyParam terminologyParam) {
    return List.of();
  }
}
