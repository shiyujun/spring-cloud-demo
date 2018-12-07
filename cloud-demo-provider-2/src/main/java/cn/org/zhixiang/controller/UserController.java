package cn.org.zhixiang.controller;

import cn.org.zhixiang.domain.User;
import cn.org.zhixiang.mq.MqMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MqMessageProducer mqMessageProducer;


    @GetMapping(value = "/getUser/{id}")
    public User getUser(@PathVariable Long id){
        User user=new User();
        user.setId(id);
        user.setName("李四");
        user.setAge(18);
        return user;
    }
    @GetMapping(value = "/getName")
    public String getName(){
        return "张三";
    }
    @GetMapping(value = "/testMq")
    public String testMq(@RequestParam("msg")String msg){
        mqMessageProducer.sendMsg(msg);
        return "发送成功";
    }
}
