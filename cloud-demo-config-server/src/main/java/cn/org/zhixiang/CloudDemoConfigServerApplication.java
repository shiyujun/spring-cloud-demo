package cn.org.zhixiang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;



@SpringBootApplication
@EnableConfigServer
public class CloudDemoConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudDemoConfigServerApplication.class, args);
	}
}
