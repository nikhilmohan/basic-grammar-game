package com.learninggrammer.basicgrammergame.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Quiz {
    private List<QuestionResponse> questions = new ArrayList<>();
}
