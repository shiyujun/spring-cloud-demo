package cn.org.zhixiang.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
@Component
public class ConnectTimeHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        long connectTime=(long)Math.random()*10;//模拟一个连接操作
        if(connectTime>3){
            //如果连接时间大于3则认为连接失败，返回状态为down
            return Health.down().withDetail("code", "504").withDetail("msg","xx应用连接超时").build();
        }
        return Health.up().build();
    }
}
