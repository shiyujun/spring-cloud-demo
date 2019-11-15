package cn.org.zhixiang.filter;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 *    自定义过滤器
 */
@Component
@Slf4j
public class TestGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {




    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            log.info("------------------------- 进入验证 --------------------------------");
            ServerHttpRequest request = exchange.getRequest();
            final String requestUri = request.getPath().pathWithinApplication().value();
            final String method = request.getMethod().toString();
            Map<String,String> map =request.getQueryParams().toSingleValueMap();
            Map HeadersMap =   request.getHeaders().toSingleValueMap();
            log.info("访问路径:" + requestUri + ",请求方式:" + method + ",请求参数:" + JSONObject.toJSONString(map) + ",请求头:" + JSONObject.toJSONString(HeadersMap));

            String bodyStr = resolveBodyFromRequest(request);
            log.info("请求体参数= "+bodyStr);

            //doSomthing


            log.info("------------------------- 验证结束 --------------------------------");


            // 进行下一个过滤器
            //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
            URI uri = request.getURI();
            ServerHttpRequest newRequest = request.mutate().uri(uri).build();
            DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
            Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

            newRequest = new ServerHttpRequestDecorator(newRequest) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return bodyFlux;
                }
            };
            //封装request，传给下一级
            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }


    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * 先从webflux中获取body，再填入request中进行传递。
     * @return 请求体
     */
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body  = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        return bodyRef.get();
    }

    /**
     * 处理异常
     * @param result
     */

    private Mono<Void> getVoidMono(ServerWebExchange serverWebExchange, String result) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }

    /**
     * 请求体数据重新封装到request
     * @param
     */
    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;

    }





}
