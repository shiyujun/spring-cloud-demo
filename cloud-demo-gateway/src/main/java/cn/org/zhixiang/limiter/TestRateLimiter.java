package cn.org.zhixiang.limiter;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * d
 *
 * @author syj
 * CreateTime 2019/11/15
 * describe:
 */
public class TestRateLimiter extends AbstractRateLimiter<RateLimiterConfig> {

    public static final String CONFIGURATION_PROPERTY_NAME = "in-memory-rate-limiter";

    private RateLimiterConfig defaultConfig;

    private final Map<String, CouchbaseProperties.Bucket> ipBucketMap = new ConcurrentHashMap<>();

    public TestRateLimiter() {
        super(RateLimiterConfig.class, CONFIGURATION_PROPERTY_NAME, null);
    }

    public TestRateLimiter(int defaultReplenishRate, int defaultBurstCapacity) {
        super(RateLimiterConfig.class, CONFIGURATION_PROPERTY_NAME, null);
        this.defaultConfig = new RateLimiterConfig();
        this.defaultConfig .setReplenishRate(defaultReplenishRate);
        this.defaultConfig .setBurstCapacity(defaultBurstCapacity);
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        RateLimiterConfig routeConfig = getConfig().get(routeId);

        //做了各种判断
        if (true) {
            // the limit is not exceeded
            return Mono.just(new Response(true, 1));
        } else {
            // limit is exceeded
            return Mono.just(new Response(false,-1));
        }
    }

}
