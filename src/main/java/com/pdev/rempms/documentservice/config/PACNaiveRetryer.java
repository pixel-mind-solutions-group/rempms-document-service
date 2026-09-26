package com.pdev.rempms.documentservice.config;

import com.pdev.rempms.documentservice.exception.BaseException;
import feign.RetryableException;
import feign.Retryer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@NoArgsConstructor
public class PACNaiveRetryer implements Retryer {

    private int retryMaxAttempt;

    private long retryInterval;

    private int attempt = 1;

    public PACNaiveRetryer(int retryMaxAttempt, Long retryInterval) {
        this.retryMaxAttempt = retryMaxAttempt;
        this.retryInterval = retryInterval;
    }

    /**
     * if retry is permitted, return (possibly after sleeping). Otherwise propagate the exception.
     *
     * @param e
     */
    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Feign retry attempt {} due to {} ", attempt, e.getMessage());
        if (attempt++ == retryMaxAttempt) {
            throw new BaseException(e.status(), e.getMessage());
        }
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new PACNaiveRetryer(3, 5000L);
    }
}

