package cn.org.zhixiang.filter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RefreshScope
@Configuration
public class GlobalRouteFilter implements GlobalFilter, Ordered {

    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = new HttpHeaders();
        BodyInserter bodyInserter = this.getBodyInserter(exchange, headers);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    String body = (String) exchange.getAttributes().get(CACHE_REQUEST_BODY_OBJECT_KEY);
                    log.info("原始请求体:{}", body);

                   /*-----------------------重新封装请求参数--------------------------*/
                    ServerHttpRequestDecorator decorator = this.getServerHttpRequestDecorator(exchange, headers, outputMessage);
                    ServerHttpResponse decoratedResponse = this.getServerHttpResponse(exchange, headers);
                    return chain.filter(exchange.mutate().request(decorator).response(decoratedResponse).build());
                }));


    }

    /**
     * 获取请求参数
     */
    private BodyInserter getBodyInserter(ServerWebExchange exchange, HttpHeaders headers) {
        ServerRequest serverRequest = new DefaultServerRequest(exchange);
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(body -> {
                    exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_KEY, body);
                    return Mono.just(body);
                });
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);

        headers.putAll(exchange.getRequest().getHeaders());
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        return bodyInserter;
    }

    /**
     * 重新包装请求体
     */
    private ServerHttpRequestDecorator getServerHttpRequestDecorator(ServerWebExchange exchange, HttpHeaders headers, CachedBodyOutputMessage outputMessage) {
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(
                exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }


        };
        return decorator;
    }

    /**
     * 重新包装响应体
     */
    private ServerHttpResponse getServerHttpResponse(ServerWebExchange exchange, HttpHeaders headers) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                Flux<DataBuffer> flux = null;
                if (body instanceof Mono) {
                    Mono<? extends DataBuffer> mono = (Mono<? extends DataBuffer>) body;
                    body = mono.flux();

                }
                if (body instanceof Flux) {
                    flux = (Flux<DataBuffer>) body;
                    return super.writeWith(flux.buffer().map(dataBuffers -> {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        dataBuffers.forEach(i -> {
                            byte[] array = new byte[i.readableByteCount()];
                            i.read(array);
                            DataBufferUtils.release(i);
                            outputStream.write(array, 0, array.length);
                            log.info("后端返回数据：{}", outputStream);
                        });
                        String result = outputStream.toString();
                        try {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        log.info("后端返回数据：{}", result);
                        return bufferFactory.wrap(result.getBytes());
                    }));
                }

                log.info("降级处理返回数据：{}" + body);
                return super.writeWith(body);
            }

        };
        return decoratedResponse;
    }

    /**
     * 异常处理
     *
     * @param result
     */

    private Mono<Void> getVoidMono(ServerWebExchange serverWebExchange, String result) {
        log.info("异常请求，请求返回结果：{}",result);
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

