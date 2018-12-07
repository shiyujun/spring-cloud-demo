package cn.org.config;

import feign.Contract;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
  @Bean
  public Contract feignContract() {

    return new Contract.Default();
  }

}
