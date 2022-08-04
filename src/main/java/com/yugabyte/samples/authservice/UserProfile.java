package com.yugabyte.samples.authservice;

import static java.lang.Boolean.TRUE;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

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
