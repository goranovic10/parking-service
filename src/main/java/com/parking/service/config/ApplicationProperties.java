package com.parking.service.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("com.parking")
public class ApplicationProperties {

  private Google google = new Google();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Google {
    private String url;
    private String apiKey;
  }
}
