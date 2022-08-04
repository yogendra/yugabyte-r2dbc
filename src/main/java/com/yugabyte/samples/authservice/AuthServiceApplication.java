package com.yugabyte.samples.authservice;

import static java.lang.Boolean.TRUE;

import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.LongStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class AuthServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }

}


@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
class UserProfile {

  @Id
  private Long id;

  @NonNull
  private String firstName;

  @NonNull
  private String lastName;

  @NonNull
  private String loginId;

  @NonNull
  private String password;

  private Boolean enabled = TRUE;
}

@Component
interface UserProfileRepository extends ReactiveCrudRepository<UserProfile, Long> {

  Flux<UserProfile> findByLastName(String lastName);

  Mono<UserProfile> findByLoginIdAndPassword(String loginId, String password);

}

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
class LoginHistory {

  public LoginHistory(@NonNull String loginId, Instant loginTime) {
    this.loginId = loginId;
    this.loginTime = loginTime;
  }

  @Id
  private Long id;

  @NonNull
  private String loginId;

  @CreatedDate
  private Instant loginTime;


}

@Component
interface LoginHistoryRepository extends ReactiveCrudRepository<LoginHistory, Long> {

  Flux<LoginHistory> findTop10ByLoginIdOrderByLoginTimeDesc(String loginId);

  @Query("SELECT * FROM login_history WHERE login_id = :loginid ORDER BY login_time desc LIMIT 10")
  Flux<LoginHistory> loginHistoryForLoginId(String loginId);
}

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

@Configuration
@Profile("local-yb")
@Slf4j
class LocalYugabyteConfig {

  @Bean
  ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    initializer.setDatabasePopulator(
      new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));

    return initializer;
  }

  @Bean
  public CommandLineRunner demo(UserProfileRepository userProfileRepository,
    LoginHistoryRepository loginHistoryRepository, LoginService loginService) {

    return (args) -> {
      initDatabaseWithUserProfiles(userProfileRepository);
      simpleUserProfileRetrieval(userProfileRepository);
      initDatabaseWithLoginHistroy(loginHistoryRepository);
      simpleLoginHistoryRetrieval(loginHistoryRepository);
    };
  }

  private void initDatabaseWithLoginHistroy(LoginHistoryRepository loginHistoryRepository) {
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

  private void simpleLoginHistoryRetrieval(LoginHistoryRepository repository) {
    // fetch login history by login id
    log.info("Login History found with findTop10ByLoginIdOrderByLoginTimeDesc('jbauer'):");
    log.info("--------------------------------------------");
    repository.findTop10ByLoginIdOrderByLoginTimeDesc("jbauer")
      .doOnNext(loginHistory -> log.info(loginHistory.toString()))
      .blockLast(Duration.ofSeconds(10));

    log.info("");

    // fetch login history by login id
    log.info("Login History found with loginHistoryForLoginId('jbauer'):");
    log.info("--------------------------------------------");
    repository.loginHistoryForLoginId("jbauer")
      .doOnNext(loginHistory -> log.info(loginHistory.toString()))
      .blockLast(Duration.ofSeconds(10));
    log.info("");

  }

  private void initDatabaseWithUserProfiles(UserProfileRepository repository) {
    // save a few customers
    repository.saveAll(Arrays.asList(
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

  private void simpleUserProfileRetrieval(UserProfileRepository repository) {

    // fetch all customers
    log.info("UserProfiles found with findAll():");
    log.info("-------------------------------");
    repository.findAll()
      .doOnNext(customer -> log.info(customer.toString()))
      .blockLast(Duration.ofSeconds(10));

    log.info("");

    // fetch an individual customer by ID
    repository.findById(1L)
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
    repository.findByLastName("Bauer")
      .doOnNext(bauer -> log.info(bauer.toString()))
      .blockLast(Duration.ofSeconds(10));
    log.info("");
  }

  private void onSaveError(Throwable e, Object o) {
    log.warn("Error [{}] on saving [{}]", e, o);
  }
}
