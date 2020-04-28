package com.learninggrammer.basicgrammergame.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class QuizAttempt {

    @NotEmpty
    private String playerId;
    @NotEmpty
    private List<AnsweredQuestion> answers = new ArrayList<AnsweredQuestion>();
    private boolean invalidateAttempt;

}
