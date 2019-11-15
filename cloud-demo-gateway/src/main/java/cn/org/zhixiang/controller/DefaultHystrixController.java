package cn.org.zhixiang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 默认降级处理
 **/
@RestController
@Slf4j
public class DefaultHystrixController {

    @RequestMapping("/defaultfallback")
    public ResponseEntity<?> defaultfallback(){
        log.info("后端服务异常,进行降级操作*******************");
        return new ResponseEntity("后端服务异常,进行降级操作*******************", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
