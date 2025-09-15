package Nagasawa.valid_X.exception.notFoundProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class NotFoundProblemException extends ProblemException {
    public NotFoundProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/notFound",
                "Not Found",
                404,
                detail,
                null
        ));
    }
}
