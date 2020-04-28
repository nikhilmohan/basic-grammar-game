package com.learninggrammer.basicgrammergame.handlers;

import com.learninggrammer.basicgrammergame.domain.*;
import com.learninggrammer.basicgrammergame.repositories.AttemptLogRepository;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
class QuizHandlerTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    QuizRepository quizRepository;
    @Autowired
    AttemptLogRepository attemptLogRepository;

    List<Question> questions = new ArrayList<Question>();


    private List<Question> data() {
        Question questionXYZ = new Question();
        questionXYZ.setStatement("A _ of flies");
        questionXYZ.setAnswer("swarm");
        questionXYZ.setId("xyz");
        questionXYZ.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        questionXYZ.setType(GrammarType.GROUPING_WORDS.getGroup());

        Question questionPQR = new Question();
        questionPQR.setStatement("A _ of bananas");
        questionPQR.setAnswer("bunch");
        questionPQR.setId("pqr");
        questionPQR.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        questionPQR.setType(GrammarType.GROUPING_WORDS.getGroup());

        questions.addAll(Arrays.asList(questionXYZ, questionPQR));
        return questions;

    }
    @BeforeEach
    void setUp() {
        quizRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(quizRepository::save)
                .blockLast();

    }

    @Test
    public void testEvaluateQuiz()  {

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setPlayerId("abcd");
        AnsweredQuestion aq = new AnsweredQuestion();
        aq.setGivenAnswer("swarm");
        aq.setId("xyz");
        aq.setResult(false);
        AnsweredQuestion aq1 = new AnsweredQuestion();
        aq1.setGivenAnswer("bunch1");
        aq1.setId("pqr");
        aq1.setResult(false);
        quizAttempt.getAnswers().addAll(Arrays.asList(aq, aq1));

        QuizAttempt quizAttemptResponse = webTestClient.post().uri("/basic-grammar/api/quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(quizAttempt), QuizAttempt.class)
                .exchange()
                .expectBody(QuizAttempt.class)
                .returnResult()
                .getResponseBody();
        quizAttemptResponse.getAnswers().forEach(System.out::println );
        assertEquals(quizAttemptResponse.getAnswers().stream().filter(aqn->aqn.getId().equals("xyz"))
                                .findAny().get().isResult(), true);
        assertEquals(quizAttemptResponse.getAnswers().stream().filter(aqn1->aqn1.getId().equals("pqr"))
                .findAny().get().isResult(), false);



    }
    @Test
    public void testEvaluateAttempt()  {
        AttemptLog attemptLog = new AttemptLog();

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setPlayerId("abcd");
        AnsweredQuestion aqn = new AnsweredQuestion();
        aqn.setGivenAnswer("swarm");
        aqn.setId("xyz");
        aqn.setResult(false);
        AnsweredQuestion aqn1 = new AnsweredQuestion();
        aqn1.setGivenAnswer("bunch1");
        aqn1.setId("pqr");
        aqn1.setResult(false);
        quizAttempt.getAnswers().addAll(Arrays.asList(aqn, aqn1));

        List<Mono<Question>> questionList = new ArrayList<>();
        quizAttempt.getAnswers().forEach((aq) -> {
                        log.info("Attempt to find question " + aq.getId());
                        log.info("Questions " + questions.size());
                        Mono<Question> questionFlux = questions.stream()
                                .filter(ques->ques.getId().equals(aq.getId()))
                                .findAny()
                                .map((Question question)-> Mono.just(question))
                                .get();
                        questionList.add(questionFlux);
                        log.info("Question list " + questionList.size());
                    });

         //   Flux<Question> qFlux = Flux.fromIterable(questions).log();

           Flux<Question> qFlux = Flux.fromIterable(questionList).flatMap(pub -> Flux.from(pub)).log();


        StepVerifier.create(qFlux)
                .expectSubscription()
          //      .expectNextCount(2L)
                .expectNextMatches(q-> q.getId().equals("xyz"))
                .expectNextMatches(q->q.getId().equals("pqr"))
                .verifyComplete();


    }


}