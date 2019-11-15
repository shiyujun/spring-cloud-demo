package cn.org.zhixiang.filter;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
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
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.ObjectUtils;
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

@Slf4j
@Configuration
public class GlobalRouteFilter implements GlobalFilter {

    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("-------------------------进入GlobalRouteFilter过滤器--------------------------------");

        ServerRequest serverRequest = new DefaultServerRequest(exchange);

        ServerHttpRequest request = exchange.getRequest();
        String header = request.getHeaders().getHost().toString().split(":")[0];
        final String requestUri = request.getPath().pathWithinApplication().value();
        final String method = request.getMethod().toString();
        Map<String, String> map = request.getQueryParams().toSingleValueMap();
        Map<String, String> HeadersMap = request.getHeaders().toSingleValueMap();


        log.info("访问的ip地址 = " + header + "访问路径:" + requestUri + ",请求方式:" + method + ",请求参数:" + JSONObject.toJSONString(map) + ",请求头:" + JSONObject.toJSONString(HeadersMap));


        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(body -> {
                    log.info("原始请求体:{}", body);
                    exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_KEY, body);
                    return Mono.just(body);
                });
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());

        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);

        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    String body = (String) exchange.getAttributes().get(CACHE_REQUEST_BODY_OBJECT_KEY);
                    log.info("请求体参数:" + body);

                    //doSomthing


                    //重新封装请求体，传到下游
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

                    /*======================================================  后端服务返回数据进行处理操作  开始 =================================================*/
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    DataBufferFactory bufferFactory = originalResponse.bufferFactory();
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        public HttpHeaders getHeaders() {
                            long contentLength = headers.getContentLength();
                            log.info("contentLength:" + contentLength);
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
//                            Flux body1 = (Flux) body;

                            Flux<DataBuffer> flux = null;
                            if (body instanceof Mono) {
                                Mono<? extends DataBuffer> mono = (Mono<? extends DataBuffer>) body;
                                body = mono.flux();

                            }

                            //修改
                            if (body instanceof Flux) {
                                flux = (Flux<DataBuffer>) body;

                                return super.writeWith(flux.buffer().map(dataBuffers -> {

                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    dataBuffers.forEach(i -> {
                                        byte[] array = new byte[i.readableByteCount()];
                                        log.info("本次读取后端返回值数据个数： " + i.readableByteCount());
                                        i.read(array);
                                        //释放内存
                                        DataBufferUtils.release(i);
                                        outputStream.write(array, 0, array.length);
                                        log.info("后端服务返回的数据：" + outputStream);

                                    });
                                    String result = outputStream.toString();
                                    try {
                                        //关闭流
                                        if (outputStream != null) {
                                            outputStream.close();
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //doSomthing
                                    log.info("返给前端数据长度：" + result.length() + " 数据：" + result);
                                    log.info("-----------------response body end-----------------");
                                    return bufferFactory.wrap(result.getBytes());
                                }));
                            }

                            log.info("降级数据：" + body);
                            return super.writeWith(body);
                        }

                    };


                    //将数据发至其他服务并进行接收返回值
                    return chain.filter(exchange.mutate().request(decorator).response(decoratedResponse).build());
                }));


    }


    /**
     * 异常处理
     *
     * @param result
     */
    private Mono<Void> getVoidMono(ServerWebExchange serverWebExchange, String result) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }

}

