package cn.org.config;

import org.springframework.context.annotation.Bean;


import feign.Contract;
import feign.Logger;

@org.springframework.context.annotation.Configuration
public class Configuration {
  @Bean
  public Contract feignContract() {

    return new Contract.Default();
  }

}
