package com.learninggrammer.basicgrammergame.config;

import com.learninggrammer.basicgrammergame.bootstrap.Dataloader;
import com.learninggrammer.basicgrammergame.handlers.QuizHandler;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.xml.crypto.Data;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> itemRoutes(QuizHandler quizHandler) {
        return RouterFunctions.route(GET("/basic-grammar/api/quiz").and(accept(MediaType.APPLICATION_JSON)), quizHandler::getQuestions)
                .andRoute(POST("/basic-grammar/api/quiz").and(accept(MediaType.APPLICATION_JSON)), quizHandler::evaluateQuiz);

    }

    @Bean
    public QuizHandler quizHandler(QuizRepository quizRepository)    {
        return new QuizHandler(quizRepository);
    }
    @Bean
    public Dataloader dataloader(QuizRepository quizRepository)    {
        return new Dataloader(quizRepository);
    }

}
