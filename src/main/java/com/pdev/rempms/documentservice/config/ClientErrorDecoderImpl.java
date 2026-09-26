package com.pdev.rempms.documentservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdev.rempms.documentservice.exception.FeignCustomException;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientErrorDecoderImpl implements ClientErrorDecoder {

    static Logger logger = LoggerFactory.getLogger(ClientErrorDecoderImpl.class);

    private final ErrorDecoder defaultErrorDecoder = new Default();

    private ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public Exception decode(String methodKey, Response response) {
        Map<String, Object> map;
        String body = "";
        try {
            body = new BufferedReader(response.body().asReader(StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            map = mapper.readValue(body, Map.class);
        } catch (Exception e) {
            logger.warn("Error body decoding exception. Body parsed as string message");
            map = new HashMap<>();
            map.put("status", response.status());
            map.put("message", body);
        }

        if (response.status() >= 400 && response.status() <= 499) {
            return new FeignCustomException(response.status(), map, map.get("message").toString());
        }

        if (response.status() == 503) {
            FeignException exception = FeignException.errorStatus(methodKey, response);
            return new RetryableException(response.status(), exception.getMessage(), response.request().httpMethod(), 1L, response.request());
        }

        if (response.status() >= 500) {
            return new FeignCustomException(response.status(), map, map.get("message").toString());
        }

        return defaultErrorDecoder.decode(methodKey, response);

    }

}
