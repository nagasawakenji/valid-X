package Nagasawa.valid_X.domain.validation;

import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.exception.validateProblems.ValidationContentProblemException;
import Nagasawa.valid_X.exception.validateProblems.ValidationMediaProblemException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TweetValidator {

    public void validateContent(String content) {
        if (content == null) throw new ValidationContentProblemException("content is null");
        if (content.length() > 280) throw new ValidationContentProblemException("content is too long");
    }

    public void validateMedia(List<Media> medias) {
        if (medias == null) return;
        if (medias.size() > 4) throw new ValidationMediaProblemException("media is too much");
    }
}
