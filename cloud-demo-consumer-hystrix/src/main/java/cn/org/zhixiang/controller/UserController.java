package cn.org.zhixiang.controller;

import cn.org.zhixiang.domain.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/getUser/{id}")
    @HystrixCommand(fallbackMethod = "getUserFallback")
    public User getUser(@PathVariable Long id){
         return restTemplate.getForObject("http://provider-demo/user/getUser/"+id,User.class);
    }
    public User getUserFallback(Long id) {
        User user = new User();
        user.setName("王五");
        return user;
    }
}
