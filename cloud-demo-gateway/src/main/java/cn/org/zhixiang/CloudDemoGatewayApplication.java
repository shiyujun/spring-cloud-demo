package cn.org.zhixiang;

import cn.org.zhixiang.limiter.TestRateLimiter;
import cn.org.zhixiang.limiter.RemoteAddrKeyResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class CloudDemoGatewayApplication {
    @Bean(RemoteAddrKeyResolver.BEAN_NAME)
    public RemoteAddrKeyResolver remoteAddrKeyResolver(){
        return  new RemoteAddrKeyResolver();
    }
    @Bean
    public RateLimiter memoryRateLimiter(){
        return new TestRateLimiter();
    }
    public static void main(String[] args) {
        SpringApplication.run(CloudDemoGatewayApplication.class, args);
    }
}
