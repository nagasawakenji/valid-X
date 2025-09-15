package Nagasawa.valid_X.exception.existingUserProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class ExistingUserProblemException extends ProblemException {
    public ExistingUserProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/existing",
                "Conflict",
                200,
                detail,
                null
        ));
    }
}
