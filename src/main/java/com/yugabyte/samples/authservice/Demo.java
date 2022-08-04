package com.yugabyte.samples.authservice;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.LongStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-yb")
@Slf4j
public class Demo implements CommandLineRunner {
  private final UserProfileRepository userProfileRepository;
  private final LoginHistoryRepository loginHistoryRepository;
  private final LoginService loginService;

  public Demo(UserProfileRepository userProfileRepository,
    LoginHistoryRepository loginHistoryRepository, LoginService loginService) {
    this.userProfileRepository = userProfileRepository;
    this.loginHistoryRepository = loginHistoryRepository;
    this.loginService = loginService;
  }

  @Override
  public void run(String... args) throws Exception {
    initDatabaseWithUserProfiles();
    simpleUserProfileRetrieval();

    initDatabaseWithLoginHistroy();
    simpleLoginHistoryRetrieval();
  }



  private void initDatabaseWithUserProfiles() {
    // save a few customers
    userProfileRepository.saveAll(Arrays.asList(
          new UserProfile("Jack", "Bauer", "jbauer", "jbauer@123"),
          new UserProfile("Chloe", "O'Brian", "cobrian", "cobrian@123"),
          new UserProfile("Kim", "Bauer", "kbauer", "kbauer@123"),
          new UserProfile("David", "Palmer", "dpalmer", "dpalmer@123"),
          new UserProfile("Michelle", "Dessler", "mdessler", "mdessler@123")
        )
      )
      .onErrorContinue(this::onSaveError)
      .blockLast(Duration.ofSeconds(10));
  }

  private void simpleUserProfileRetrieval() {

    // fetch all customers
    log.info("UserProfiles found with findAll():");
    log.info("-------------------------------");
    userProfileRepository.findAll()
      .doOnNext(customer -> log.info(customer.toString()))
      .blockLast(Duration.ofSeconds(10));

    log.info("");

    // fetch an individual customer by ID
    userProfileRepository.findById(1L)
      .doOnNext(customer -> {
        log.info("Customer found with findById(1L):");
        log.info("--------------------------------");
        log.info(customer.toString());
        log.info("");
      })
      .block(Duration.ofSeconds(10));

    // fetch customers by last name
    log.info("Customer found with findByLastName('Bauer'):");
    log.info("--------------------------------------------");
    userProfileRepository.findByLastName("Bauer")
      .doOnNext(bauer -> log.info(bauer.toString()))
      .blockLast(Duration.ofSeconds(10));
    log.info("");
  }
  private void initDatabaseWithLoginHistroy() {
    Instant start = Instant.parse("2022-08-01T09:00:00.00Z");
    var loginHistoryList = LongStream
      .range(1, 20)
      .mapToObj(i -> start.minus(i, ChronoUnit.DAYS))
      .map(time -> new LoginHistory("jbauer", time))
      .toList();

    loginHistoryRepository.saveAll(loginHistoryList)
      .onErrorContinue(this::onSaveError)
      .blockLast(Duration.ofSeconds(10));
  }

  private void simpleLoginHistoryRetrieval() {
    // fetch login history by login id
    log.info("Login History found with findTop10ByLoginIdOrderByLoginTimeDesc('jbauer'):");
    log.info("--------------------------------------------");
    loginHistoryRepository.findTop10ByLoginIdOrderByLoginTimeDesc("jbauer")
      .doOnNext(loginHistory -> log.info(loginHistory.toString()))
      .blockLast(Duration.ofSeconds(10));

    log.info("");

    // fetch login history by login id
    log.info("Login History found with loginHistoryForLoginId('jbauer'):");
    log.info("--------------------------------------------");
    loginHistoryRepository.loginHistoryForLoginId("jbauer")
      .doOnNext(loginHistory -> log.info(loginHistory.toString()))
      .blockLast(Duration.ofSeconds(10));
    log.info("");

  }
  private void onSaveError(Throwable e, Object o) {
    log.warn("Error [{}] on saving [{}]", e, o);
  }
}
