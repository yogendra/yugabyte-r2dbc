package com.yugabyte.samples.authservice;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

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
