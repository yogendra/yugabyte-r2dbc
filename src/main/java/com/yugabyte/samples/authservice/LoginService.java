package com.yugabyte.samples.authservice;

import java.time.Instant;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class LoginService {

  LoginHistoryRepository loginHistoryRepository;
  UserProfileRepository userProfileRepository;

  public LoginService(LoginHistoryRepository loginHistoryRepository,
    UserProfileRepository userProfileRepository) {
    this.loginHistoryRepository = loginHistoryRepository;
    this.userProfileRepository = userProfileRepository;
  }

  public Mono<UserProfile> loginUser(String login, String password) {
    return loginUser(login, password, Instant.now());
  }

  // Just for demo
  public Mono<UserProfile> loginUser(String login, String password, Instant time) {

    return userProfileRepository
      .findByLoginIdAndPassword(login, password)
      .doOnSuccess(userProfile -> {
        var history = new LoginHistory(userProfile.getLoginId(), time);
        loginHistoryRepository.save(history);
      });
  }

  public Flux<LoginHistory> loginHistory(String loginId) {
    return loginHistoryRepository.loginHistoryForLoginId(loginId);
  }
}
