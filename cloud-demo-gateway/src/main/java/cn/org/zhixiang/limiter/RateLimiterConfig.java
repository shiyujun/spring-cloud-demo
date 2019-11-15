package cn.org.zhixiang.limiter;

import lombok.Data;

/**
 * d
 *
 * @author syj
 * CreateTime 2019/11/15
 * describe:
 */
@Data
public class RateLimiterConfig {
    private int replenishRate;

    private int burstCapacity = 0;
}
