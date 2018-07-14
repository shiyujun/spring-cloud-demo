package cn.org.zhixiang.controller;

import cn.org.zhixiang.domain.User;
import cn.org.zhixiang.feign.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserFeignClient userFeignClient;

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable Long id){
         return userFeignClient.getUser(id);
    }
}
