package com.learninggrammer.basicgrammergame.handlers;

import com.learninggrammer.basicgrammergame.domain.Question;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class QuizHandler {
    private QuizRepository quizRepository;
    public QuizHandler(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public Mono<ServerResponse> getQuestions(ServerRequest serverRequest) {
        Flux<Question> questions = quizRepository.findAll().take(2);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(questions, Question.class);
    }

    public Mono<ServerResponse> evaluateQuiz(ServerRequest serverRequest) {

        return ServerResponse.ok().build();
    }
}
