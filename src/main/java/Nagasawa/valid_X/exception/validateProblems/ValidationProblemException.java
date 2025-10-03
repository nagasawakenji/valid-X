package Nagasawa.valid_X.exception.validateProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class ValidationProblemException extends ProblemException {
    public ValidationProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/notFound",
                "Not Found",
                400,
                detail,
                null
        ));
    }

}
