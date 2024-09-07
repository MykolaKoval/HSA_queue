package com.course.hsa.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String EVENTS_CHANNEL = "events";
    private static final String EVENTS_LIST = "events-list";
    @Value("${app.processing.concurrency}")
    private final Integer concurrency;
    private final JedisPooled jedis;
    private final ScheduledExecutorService scheduler;

    // ~ 3x slower than async
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
        jedis.publish(EVENTS_CHANNEL, UUID.randomUUID().toString());
        if (counter.incrementAndGet() == publishQty) {
            log.info("Published qty: {}", publishQty);
        }
    }

    @SneakyThrows
    public void publishWithFixedThroughput(Long throughput) {
        // start work
        AtomicLong counter = new AtomicLong(0);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        var scheduledTask = scheduler.scheduleAtFixedRate(publish(executor, counter, throughput), 0, 1, TimeUnit.SECONDS);

        // interrupt work
        Runnable cancelTask = () -> {
            log.info("Publish completed");
            scheduledTask.cancel(true);
        };
        scheduler.schedule(cancelTask, 30, TimeUnit.SECONDS);
    }

    public void subscribe(Long notifyQty) {
        log.info("Subscribing channel={}, notify on qty={}", EVENTS_CHANNEL, notifyQty);
        AtomicLong counter = new AtomicLong(0);
        var pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (counter.incrementAndGet() == notifyQty) {
                    log.info("Consumed qty: {}", notifyQty);
                }
            }
        };
        CompletableFuture.runAsync(() -> jedis.subscribe(pubSub, EVENTS_CHANNEL));
    }

    // FIFO queue
    public void enqueueAsync(Long qty) {
        log.info("Queueing async started");
        AtomicLong counter = new AtomicLong(0);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        enqueue(executor, counter, qty).run();
        log.info("Queueing async completed");
    }

    private Runnable enqueue(ExecutorService executor, AtomicLong counter, Long qty) {
        return () -> {
            for (int i = 0; i < qty; i++) {
                executor.submit(() -> enqueueSingle(counter, qty));
            }
            log.info("Submitted {} events to list", qty);
        };
    }

    private void enqueueSingle(AtomicLong counter, Long publishQty) {
        jedis.rpush(EVENTS_LIST, UUID.randomUUID().toString());
        if (counter.incrementAndGet() == publishQty) {
            log.info("Pushed qty: {}", publishQty);
        }
    }

    public void dequeue(Long notifyQty) {
        log.info("Reading queue={}, notify on qty={}", EVENTS_LIST, notifyQty);
        AtomicLong counter = new AtomicLong(0);
        while(true) {
            jedis.lpop(EVENTS_LIST);
            if (counter.incrementAndGet() == notifyQty) {
                log.info("Read qty: {}", notifyQty);
                break;
            }
        }
    }
}
