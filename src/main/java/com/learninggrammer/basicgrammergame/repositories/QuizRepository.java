package com.learninggrammer.basicgrammergame.repositories;

import com.learninggrammer.basicgrammergame.domain.Question;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface QuizRepository extends ReactiveMongoRepository<Question, String> {
}
