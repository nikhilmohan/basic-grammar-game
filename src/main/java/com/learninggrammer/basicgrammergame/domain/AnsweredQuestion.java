package com.learninggrammer.basicgrammergame.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class AnsweredQuestion {
    @NotEmpty
    private String id;
    @NotEmpty
    private String givenAnswer;
    private boolean result;
}
