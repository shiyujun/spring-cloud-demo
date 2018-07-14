package cn.org.zhixiang.feign;

import cn.org.zhixiang.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


//@FeignClient(name = "provider-demo", fallback = HystrixClientFallback.class)
@FeignClient(name = "provider-demo", fallbackFactory = HystrixClientFactory.class)
public interface UserFeignClient {

  @GetMapping (value = "/user/getUser/{id}")
  public User getUser(@PathVariable("id") Long id);


}
