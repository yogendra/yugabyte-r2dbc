package com.yugabyte.samples.authservice;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
interface LoginHistoryRepository extends ReactiveCrudRepository<LoginHistory, Long> {

  Flux<LoginHistory> findTop10ByLoginIdOrderByLoginTimeDesc(String loginId);

  @Query("SELECT * FROM login_history WHERE login_id = :loginid ORDER BY login_time desc LIMIT 10")
  Flux<LoginHistory> loginHistoryForLoginId(String loginId);
}
