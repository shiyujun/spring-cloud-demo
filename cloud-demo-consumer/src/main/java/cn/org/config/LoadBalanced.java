package cn.org.config;

import com.netflix.loadbalancer.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalanced {
    @Bean
    public IRule ribbonRule() {
        return new RoundRobinRule();                //轮训
       // return new WeightedResponseTimeRule();    //加权权重
       //return new RetryRule();                    //带有重试机制的轮训
       //return new RandomRule();                   //随机
       //return new TestRule();                     //自定义规则
    }
}
