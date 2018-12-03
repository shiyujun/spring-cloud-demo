package cn.org.zhixiang.mq;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * d
 *
 * @author syj
 * CreateTime 2018/12/03
 * describe:
 */
public interface  MqMessageSource {

    String TEST_IN_PUT = "testInPut";

    @Input(TEST_IN_PUT)
    SubscribableChannel testInPut();

}
