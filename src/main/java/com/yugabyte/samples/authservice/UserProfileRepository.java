package com.yugabyte.samples.authservice;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
interface UserProfileRepository extends ReactiveCrudRepository<UserProfile, Long> {

  Flux<UserProfile> findByLastName(String lastName);

  Mono<UserProfile> findByLoginIdAndPassword(String loginId, String password);

}
