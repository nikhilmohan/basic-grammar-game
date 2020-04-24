package com.learninggrammer.basicgrammergame.bootstrap;

import com.learninggrammer.basicgrammergame.domain.GrammarType;
import com.learninggrammer.basicgrammergame.domain.Question;
import com.learninggrammer.basicgrammergame.repositories.QuizRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Slf4j
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
        question.setType(GrammarType.GROUPING_WORDS);


        Question question1 = new Question();
        question1.setStatement("A _ of flies");
        question1.setAnswer("swarm");
        question1.setOptions(Arrays.asList("batch", "bunch", "flock", "swarm"));
        question1.setType(GrammarType.GROUPING_WORDS);



        Question question2 = new Question();
        question2.setStatement("Plural of sheep");
        question2.setAnswer("sheep");
        question2.setType(GrammarType.PLURALS);

        Flux.fromIterable(Arrays.asList(question, question1, question2))
                .flatMap(quizRepository::save)
                .log()
                .blockLast();

        log.info("Saved to db " + quizRepository.count().block());



    }
}
