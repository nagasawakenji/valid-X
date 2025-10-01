package Nagasawa.valid_X.exception.invalidProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class InvalidProblemException extends ProblemException {
    public InvalidProblemException(String detail) {
      super(new Problem(
              "https://api.example.com/problems/invalid",
              "Invalid",
              401,
              detail,
              null
              ));
    }
}
