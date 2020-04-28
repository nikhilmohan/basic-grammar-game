package com.learninggrammer.basicgrammergame.handlers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BGGException extends RuntimeException {
    private String message;
    private int status;
    public BGGException(int status, String message) {
        log.error("Error " + message);
        this.message = message;
        this.status = status;

    }
}
