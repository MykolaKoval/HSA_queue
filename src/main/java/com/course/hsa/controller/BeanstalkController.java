package com.course.hsa.controller;

import com.course.hsa.service.BeanstalkService;
import com.course.hsa.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beanstalk")
@RequiredArgsConstructor
public class BeanstalkController {

    private final BeanstalkService beanstalkService;

    @PostMapping(path = "/publish")
    public void publish(@RequestParam("qty") Long qty) {
        beanstalkService.publish(qty);
    }

    @PostMapping(path = "/publish-async")
    public void publishAsync(@RequestParam("qty") Long qty) {
        beanstalkService.publishAsync(qty);
    }

    @PostMapping(path = "/subscribe")
    public void subscribe(@RequestParam("notifyQty") Long notifyQty) {
        beanstalkService.subscribe(notifyQty);
    }

}
