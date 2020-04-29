package com.learninggrammer.basicgrammergame.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "basic_quiz")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Question {

    @Id
    private String id;
    private String questionId;
    private String statement;
    private List<String> options = new ArrayList<String>();
    private String answer;

    private String type;


}
