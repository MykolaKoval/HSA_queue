package com.course.hsa.service;

import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.course.hsa.config.BeanstalkConfig.EVENTS_TUBE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeanstalkService {

    private static final int JOB_PRIORITY = 1;
    private static final int JOB_DELAY_SEC = 1;
    private static final int JOB_RUN_TIMEOUT_SEC = 60;
    private static final int JOB_RESERVE_TIMEOUT_SEC = 60;
    @Value("${app.processing.concurrency}")
    private final Integer concurrency;
    private final JobProducer jobProducer;
    private final JobConsumer jobConsumer;

    public void publish(Long qty) {
        log.info("Publishing started");
        AtomicLong counter = new AtomicLong(0);
        for (int i = 0; i < qty; i++) {
            publishSingle(counter, qty);
        }
        log.info("Publishing completed");
    }

    public void publishAsync(Long qty) {
        log.info("Publishing async started");
        AtomicLong counter = new AtomicLong(0);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        publish(executor, counter, qty).run();
        log.info("Publishing async completed");
    }

    private Runnable publish(ExecutorService executor, AtomicLong counter, Long qty) {
        return () -> {
            for (int i = 0; i < qty; i++) {
                executor.submit(() -> publishSingle(counter, qty));
            }
            log.info("Submitted {} events", qty);
        };
    }

    private void publishSingle(AtomicLong counter, Long publishQty) {
        jobProducer.putJob(JOB_PRIORITY, JOB_DELAY_SEC, JOB_RUN_TIMEOUT_SEC, UUID.randomUUID().toString().getBytes());
        if (counter.incrementAndGet() == publishQty) {
            log.info("Published qty: {}", publishQty);
        }
    }

    public void subscribe(Long notifyQty) {
        log.info("Subscribing tube={}, notify on qty={}", EVENTS_TUBE, notifyQty);
        AtomicLong counter = new AtomicLong(0);
        while(true) {
            var job = jobConsumer.reserveJob(JOB_RESERVE_TIMEOUT_SEC);
            jobConsumer.deleteJob(job.getId());
            if (counter.incrementAndGet() == notifyQty) {
                log.info("Consumed qty: {}", notifyQty);
                break;
            }
        }
    }
}
