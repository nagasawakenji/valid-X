package Nagasawa.valid_X.exception.goneProblems;

import Nagasawa.valid_X.domain.dto.Problem;
import Nagasawa.valid_X.exception.ProblemException;

// Status 410の抽象クラス
public class GoneProblemException extends ProblemException {
    // detailのみを受け取る
    public GoneProblemException(String detail) {
        super(new Problem(
                "https://api.example.com/problems/gone",
                "Gone",
                410,
                detail,
                null
        ));
    }
}
