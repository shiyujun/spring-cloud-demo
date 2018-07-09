package cn.org.zhixiang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CloudDemoProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudDemoProviderApplication.class, args);
	}
}
