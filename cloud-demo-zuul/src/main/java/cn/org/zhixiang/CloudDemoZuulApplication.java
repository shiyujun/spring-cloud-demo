package cn.org.zhixiang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class CloudDemoZuulApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudDemoZuulApplication.class, args);
    }
}
