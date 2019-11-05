package cn.shiyujun.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * d
 *
 * @author syj
 * CreateTime 2019/10/21
 * describe:
 */
public class HelloCommand extends HystrixCommand<String> {

    public HelloCommand() {
        super(HystrixCommandGroupKey.Factory.asKey("test"));
    }

    @Override
    protected String run() throws Exception {
        return "sucess";
    }

    @Override
    protected String getFallback() {
        System.out.println("执行了回退方法");
        return "error";
    }

}
