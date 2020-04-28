package com.learninggrammer.basicgrammergame.handlers;

import com.learninggrammer.basicgrammergame.domain.*;
import com.learninggrammer.basicgrammergame.repositories.AttemptLogRepository;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.service.Server;


import javax.validation.Validator;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
public class QuizHandler {
    private static final int QUIZ_SIZE = 2;
    private QuizRepository quizRepository;
    private AttemptLogRepository attemptLogRepository;
    private boolean evaluationStatus = false;
    private Validator validator;
    public QuizHandler(QuizRepository quizRepository, AttemptLogRepository attemptLogRepository, Validator validator) {
        this.quizRepository = quizRepository;
        this.attemptLogRepository = attemptLogRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> getQuestions(ServerRequest serverRequest) {
        Mono<Quiz> questions;
        try {
            Quiz quiz = new Quiz();
            log.info("Got request");
            questions = quizRepository.findAll()
                    .take(QUIZ_SIZE)
                    //     .onErrorResume(Mono.error(()->new BGGException("Data not found"))
                    .map(question -> {
                        QuestionResponse qResponse = new QuestionResponse();
                        qResponse.setId(question.getId());
                        question.getOptions().forEach(option -> qResponse.getOptions().add(option));
                        qResponse.setStatement(question.getStatement());
                        qResponse.setType(question.getType());
                        quiz.getQuestions().add(qResponse);

                        return qResponse;
                    }).then(Mono.just(quiz));
        } catch(RuntimeException e) {
            throw new BGGException(500, e.getMessage());
        }
        return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(questions, Quiz.class);
    }

    private boolean isRequestInvalid(QuizAttempt qa)    {
        return !validator.validate(qa).isEmpty()   ||
                qa.getAnswers().stream().anyMatch(aq->!validator.validate(aq).isEmpty()) ||
                qa.getAnswers().size() != QUIZ_SIZE;
    }

    private Mono<QuizAttempt> evaluateAttempt(Mono<QuizAttempt> quizAttempt)  {
        AttemptLog attemptLog = new AttemptLog();
        attemptLog.setTimestamp(LocalDateTime.now());


        return quizAttempt.flatMap(qa -> {
            if (isRequestInvalid(qa))  {
                log.info("Validator found something");
                qa.setInvalidateAttempt(true);
                return Mono.just(qa);
            }
            attemptLog.setPlayerId(qa.getPlayerId());
            Stream<Mono<Question>> monoStream = qa.getAnswers().stream()
                    .flatMap(aq->Stream.of(quizRepository.findById(aq.getId()).log()
                            .switchIfEmpty(Mono.error(new RuntimeException("no data")))));
            Flux<Question> questionFlux = Flux.fromStream(monoStream).flatMap(questionMono -> Flux.from(questionMono));
            questionFlux
                    .subscribe(question -> {
                        log.info("Evaluating answer " + question.getId() );
                        qa.getAnswers().stream().filter(aq -> question.getId().equalsIgnoreCase(aq.getId()))
                                .findAny()
                                .ifPresent(answeredQuestion -> answeredQuestion.setResult
                                        (question.getAnswer().equalsIgnoreCase(answeredQuestion.getGivenAnswer())));

                    },(e -> qa.setInvalidateAttempt(true)),
                    () -> {
                        attemptLog.setResult(calculateResult(qa.getAnswers()));
                    });


            return  attemptLogRepository.save(attemptLog).log()
                    .flatMap(attemptLog1 -> Mono.just(qa));
        });

    }



    private Double calculateResult(List<AnsweredQuestion> answers) {
       double correctCount = answers.
                                stream()
                                .filter(answer -> answer.isResult())
                                .count();
       log.info("Correct answer count " + correctCount);
       return correctCount / (double)answers.size();

    }

    public Mono<ServerResponse> evaluateQuiz(ServerRequest serverRequest) {

        log.info("Evaluation started");
        Mono<QuizAttempt> quizAttemptMono = serverRequest.bodyToMono(QuizAttempt.class).log();

        return evaluateAttempt(quizAttemptMono).flatMap(quizAttempt -> {
            log.info("Received qa " + quizAttempt);
            if (quizAttempt.isInvalidateAttempt()) {
                throw new BGGException(400, "invalid quiz submitted!");
            }
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromValue(quizAttempt));

        }).switchIfEmpty(ServerResponse.notFound().build());


    }

}
