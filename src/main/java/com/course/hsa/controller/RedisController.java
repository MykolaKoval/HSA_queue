package com.course.hsa.controller;

import com.course.hsa.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping(path = "/publish")
    public void publish(@RequestParam("qty") Long qty) {
        redisService.publish(qty);
    }

    @PostMapping(path = "/publish-async")
    public void publishAsync(@RequestParam("qty") Long qty) {
        redisService.publishAsync(qty);
    }

    @PostMapping(path = "/enqueue-async")
    public void enqueueAsync(@RequestParam("qty") Long qty) {
        redisService.enqueueAsync(qty);
    }

    @PostMapping(path = "/subscribe")
    public void subscribe(@RequestParam("notifyQty") Long notifyQty) {
        redisService.subscribe(notifyQty);
    }

    @PostMapping(path = "/dequeue")
    public void dequeue(@RequestParam("notifyQty") Long notifyQty) {
        redisService.dequeue(notifyQty);
    }

}
