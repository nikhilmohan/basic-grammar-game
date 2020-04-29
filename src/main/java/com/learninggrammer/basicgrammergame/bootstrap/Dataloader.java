package com.learninggrammer.basicgrammergame.bootstrap;

import com.learninggrammer.basicgrammergame.domain.AnsweredQuestion;
import com.learninggrammer.basicgrammergame.domain.GrammarType;
import com.learninggrammer.basicgrammergame.domain.Question;
import com.learninggrammer.basicgrammergame.domain.QuizAttempt;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Slf4j

@Profile("!test")
public class Dataloader implements CommandLineRunner {

    QuizRepository quizRepository;

    public Dataloader(QuizRepository quizRepository)    {
        this.quizRepository = quizRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        Question question = new Question();
        question.setStatement("A _ of bananas");
        question.setAnswer("bunch");
        question.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        question.setType(GrammarType.GROUPING_WORDS.getGroup());


        Question question1 = new Question();
        question1.setStatement("A _ of flies");
        question1.setAnswer("swarm");
        question1.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        question1.setType(GrammarType.GROUPING_WORDS.getGroup());


        Question question2 = new Question();
        question2.setStatement("Plural of sheep");
        question2.setAnswer("sheep");
        question2.setType(GrammarType.PLURALS.getGroup());

        log.info("Question! " + question2);

        Flux.fromIterable(Arrays.asList(question, question1, question2))
                .flatMap(quizRepository::save)
                .log();
           //     .blockLast();

        log.info("Saved to DB " + quizRepository.count());

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setPlayerId("ABC");
        AnsweredQuestion aq1 = new AnsweredQuestion();
        aq1.setResult(false);
        aq1.setGivenAnswer("bunch");
        aq1.setId("5ea42344a0174c2af07feed6");

        AnsweredQuestion aq2 = new AnsweredQuestion();
        aq2.setResult(false);
        aq2.setGivenAnswer("bunch");
        aq2.setId("pqr");

        Question questionXYZ = new Question();
        questionXYZ.setStatement("A _ of flies");
        questionXYZ.setAnswer("swarm");
        questionXYZ.setId("5ea42344a0174c2af07feed6");
        questionXYZ.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        questionXYZ.setType(GrammarType.GROUPING_WORDS.getGroup());

        Question questionPQR = new Question();
        questionPQR.setStatement("A _ of flies");
        questionPQR.setAnswer("bunch");
        questionPQR.setId("pqr");
        questionPQR.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        questionPQR.setType(GrammarType.GROUPING_WORDS.getGroup());


        quizAttempt.getAnswers().addAll(Arrays.asList(aq1, aq2));
        Flux<Question> questionFlux = Flux.fromIterable(Arrays.asList(questionXYZ, questionPQR));
        questionFlux.subscribe(ques -> {
            log.info("Question in question " + ques.getId());
            quizAttempt.getAnswers().stream().filter(q -> q.getId().equals(ques.getId()))
                    .findAny().ifPresent(ansqs -> {
                log.info("Streamed ques" + ques.getAnswer());
                log.info("given answer" + ansqs.getGivenAnswer());
                ansqs.setResult(ques.getAnswer().equalsIgnoreCase(ansqs.getGivenAnswer()));
                log.info("result" + ansqs.isResult());

            });


        });
    }
}
