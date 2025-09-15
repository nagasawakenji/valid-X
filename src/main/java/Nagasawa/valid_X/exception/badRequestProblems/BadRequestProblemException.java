package Nagasawa.valid_X.exception.badRequestProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class BadRequestProblemException extends ProblemException {
    public BadRequestProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/bad_request",
                "Bad Request",
                400,
                detail,
                null
        ));
    }
}
