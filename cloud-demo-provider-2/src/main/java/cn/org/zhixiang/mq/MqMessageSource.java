package cn.org.zhixiang.mq;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * d
 *
 * @author syj
 * CreateTime 2018/12/03
 * describe:
 */
public interface  MqMessageSource {

    String TEST_OUT_PUT = "testOutPut";

    @Output(TEST_OUT_PUT)
    MessageChannel testOutPut();

}
