package cn.org.zhixiang.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

/**
 * d
 *
 * @author syj
 * CreateTime 2018/12/03
 * describe:
 */
@EnableBinding(MqMessageSource.class)
public class MqMessageProducer {
    @Autowired
    @Input(MqMessageSource.TEST_OUT_PUT)
    private MessageChannel channel;


    public void sendMsg(String msg) {
        channel.send(MessageBuilder.withPayload(msg).build());
        System.err.println("消息发送成功："+msg);
    }
}
