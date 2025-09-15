package Nagasawa.valid_X.exception;

import Nagasawa.valid_X.domain.dto.Problem;

public class ProblemException extends RuntimeException {

    private final Problem problem;
    public ProblemException(Problem problem) {
        this.problem = problem;
    }

    public Problem getProblem() {
        return problem;
    }
}
