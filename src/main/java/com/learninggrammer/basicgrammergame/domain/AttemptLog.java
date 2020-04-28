package com.learninggrammer.basicgrammergame.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@ToString
public class AttemptLog {
    @Id
    private String id;
    private String playerId;
    private LocalDateTime timestamp;
    private double result;
}
