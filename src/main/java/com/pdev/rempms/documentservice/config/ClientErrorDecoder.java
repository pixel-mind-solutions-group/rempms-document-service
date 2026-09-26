package com.pdev.rempms.documentservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;

public interface ClientErrorDecoder extends ErrorDecoder {

    @SneakyThrows
    Exception decode(String methodKey, Response response);
}
