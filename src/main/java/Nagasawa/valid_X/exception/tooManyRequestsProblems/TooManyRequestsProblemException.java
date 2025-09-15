package Nagasawa.valid_X.exception.tooManyRequestsProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class TooManyRequestsProblemException extends ProblemException {
    public TooManyRequestsProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/tooManyRequest",
                "Too Many Requests",
                429,
                detail,
                null
        ));
    }
}
