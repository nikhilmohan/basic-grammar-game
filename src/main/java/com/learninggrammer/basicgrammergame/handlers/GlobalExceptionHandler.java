package com.learninggrammer.basicgrammergame.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@Slf4j
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ApplicationContext applicationContext,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, new ResourceProperties(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);

    }
    private Mono<ServerResponse> renderErrorResponse(ServerRequest serverRequest)   {

        Map<String, Object> attributesMap = getErrorAttributes(serverRequest, false);
        log.error("Error attributes map " + attributesMap);
        attributesMap.put("status", 400);
        attributesMap.put("error", attributesMap.get("message"));
        //Mono<ErrorDescription> errorMono = Mono.just(new ErrorDescription(attributesMap.get("message").toString()));
        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(attributesMap));
    }

}
