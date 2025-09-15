package Nagasawa.valid_X.exception.conflictProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

public class ConflictProblemException extends ProblemException {
    public ConflictProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/conflict",
                "Conflict",
                409,
                detail,
                null
        ));
    }
}
