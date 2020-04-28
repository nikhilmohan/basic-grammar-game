package com.learninggrammer.basicgrammergame.repositories;

import com.learninggrammer.basicgrammergame.domain.AttemptLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AttemptLogRepository extends ReactiveMongoRepository<AttemptLog, String> {
}
