package com.learninggrammer.basicgrammergame.handlers;

import com.learninggrammer.basicgrammergame.domain.*;
import com.learninggrammer.basicgrammergame.repositories.AttemptLogRepository;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



import javax.validation.Validator;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
public class QuizHandler {
    private static final int QUIZ_SIZE = 2;
    private static final int QUESTION_ID_BEGIN = 100;
    private static final int QUESTION_ID_END = 153;

    private QuizRepository quizRepository;
    private AttemptLogRepository attemptLogRepository;
    private boolean evaluationStatus = false;
    private Validator validator;
    public QuizHandler(QuizRepository quizRepository, AttemptLogRepository attemptLogRepository, Validator validator) {
        this.quizRepository = quizRepository;
        this.attemptLogRepository = attemptLogRepository;
        this.validator = validator;
    }
    private boolean pickQuestion(Question question, List<Integer> indices)
    {
       log.info("indices : " + indices + " - " + question.getQuestionId());
       return indices.stream().anyMatch(x -> x == Integer.parseInt(question.getQuestionId()));
    }
    public Mono<ServerResponse> getQuestions(ServerRequest serverRequest) {

        List<Integer> listToPick = new Random().ints(QUIZ_SIZE, QUESTION_ID_BEGIN, QUESTION_ID_END)
                .boxed()
                .collect(toList());

        Quiz quiz;
        try {
            quiz = new Quiz();
            log.info("Got request");
            return quizRepository.findAll().log()
                    .filter(question -> pickQuestion(question, listToPick))
                    .map(question -> {
                        QuestionResponse qResponse = new QuestionResponse();
                        qResponse.setId(question.getId());
                        question.getOptions().forEach(option -> qResponse.getOptions().add(option));
                        qResponse.setStatement(question.getStatement());
                        qResponse.setType(question.getType());
                        quiz.getQuestions().add(qResponse);
                        log.info("Retrieved question " + qResponse.getStatement());
                        return qResponse;
                    }).then(Mono.just(quiz))
            .flatMap(quiz1 -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(quiz1), Quiz.class));
        } catch(RuntimeException e) {
            throw new BGGException(500, e.getMessage());
        }

    }

    private boolean isRequestInvalid(QuizAttempt qa)    {
        return !validator.validate(qa).isEmpty()   ||
                qa.getAnswers().stream().anyMatch(aq->!validator.validate(aq).isEmpty()) ||
                qa.getAnswers().size() != QUIZ_SIZE;
    }

    private Mono<QuizAttempt> evaluateAttempt(Mono<QuizAttempt> quizAttempt) {
        return quizAttempt.flatMap(qa -> {
            if (isRequestInvalid(qa)) {
                log.info("Validator found something");
                qa.setInvalidateAttempt(true);
                return Mono.just(qa);
            }
            Stream<Mono<Question>> monoStream = qa.getAnswers().stream()
                    .flatMap(aq -> Stream.of(quizRepository.findById(aq.getId()).log()
                            .switchIfEmpty(Mono.error(new RuntimeException("no data")))));
            return Flux.fromStream(monoStream).flatMap(questionMono -> Flux.from(questionMono))
                    .reduce(qa, (  qAttempt,   question)-> {
                        qAttempt.getAnswers().stream().filter(aq -> question.getId().equalsIgnoreCase(aq.getId()))
                                .findAny()
                                .ifPresent(answeredQuestion -> answeredQuestion.setResult
                                        (question.getAnswer().equalsIgnoreCase(answeredQuestion.getGivenAnswer())));
                        return qAttempt;
                    })
                    .flatMap(quizAttempt1 -> {
                        AttemptLog attemptLog = new AttemptLog();
                        attemptLog.setTimestamp(LocalDateTime.now());
                        attemptLog.setPlayerId(quizAttempt1.getPlayerId());
                        attemptLog.setResult(calculateResult(quizAttempt1.getAnswers()));
                        log.info("Attempt log " + attemptLog);
                        return attemptLogRepository.save(attemptLog).log();
                    }).flatMap(attemptLog1 -> {
                        log.info("Reached here " + attemptLog1.getId());
                        return Mono.just(qa);
                    });
        });
    }

    private Double calculateResult(List<AnsweredQuestion> answers) {
       double correctCount = answers.
                                stream()
                                .filter(answer -> answer.isResult())
                                .count();
       log.info("Correct answer count " + correctCount);


       return 100.00d * (correctCount / (double)answers.size()) ;

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
