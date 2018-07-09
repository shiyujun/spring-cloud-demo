package cn.org.zhixiang.controller;

import cn.org.zhixiang.domain.User;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable Long id){
        List<ServiceInstance> list = discoveryClient.getInstances("eureka-server");
        if (list != null && list.size() > 0 ) {
            System.out.println(list.get(0).getUri());
        }
        return restTemplate.getForObject("http://localhost:8078/user/getUser/"+id,User.class);
    }
}
