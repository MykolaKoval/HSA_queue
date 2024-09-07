# HSA queue
Queue capabilities for Redis and Beanstalkd 

Connect to container
```
docker exec -it {container_name} /bin/sh
```

<h4>Beanstalkd</h4>

* [Protocol description](https://github.com/beanstalkd/beanstalkd/blob/master/doc/protocol.txt)
* [Beanstalkd client libs](https://github.com/beanstalkd/beanstalkd/wiki/Client-Libraries)
* [Run options](https://github.com/touchifyapp/docker-beanstalkd)
* [Aurora admin console](https://github.com/xuri/aurora)

<h4>Redis Pub/Sub</h4>

* [Documentation](https://redis.io/glossary/pub-sub/)
* [Pub/Sub in Depth](https://medium.com/@joudwawad/redis-pub-sub-in-depth-d2c6f4334826)

Redis Pub/Sub is synchronous. Subscribers and publishers must be connected at the same time in order for the message to be delivered.

Redis Pub/Sub is considered a “Fire & Forget” messaging system because it does not provide an explicit acknowledgment mechanism for confirming that a message was received by the receiver.
```
127.0.0.1:6379> subscribe {channel}
```

<h3>Task</h3>

1. Compare publish to queue throughput (msg/sec)
2. Compare subscribe from queue throughput (msg/sec)

<h3>Results</h3>

<h4>1. Beanstalkd</h4>

Publish and consume
```
POST http://localhost:8080/api/beanstalk/publish-async?qty=1000000
POST http://localhost:8080/api/beanstalk/subscribe?notifyQty=1000000
```
| Messages (qty) | Publish time (sec) | Publish throughput (msg/sec) | Consume time (sec) | Consume throughput (msg/sec) |
|----------------|--------------------|------------------------------|--------------------|------------------------------|
| 10000          | 0.23               | 43478                        | 1.30               | 7692                         |
| 100000         | 2.97               | 33670                        | 8.08               | 12376                        |
| 1000000        | 24                 | 41666                        | 76.67              | 13042                        |

<h4>2. Redis (AOF, fsync = everysec)</h4>

```
appendonly yes
appendfsync everysec
```
Publish and consume
```
POST http://localhost:8080/api/redis/publish-async?qty=1000000
POST http://localhost:8080/api/redis/subscribe?notifyQty=1000000
```
| Messages (qty) | Publish time (sec) | Publish throughput (msg/sec) | Consume time (sec) | Consume throughput (msg/sec) |
|----------------|--------------------|------------------------------|--------------------|------------------------------|
| 10000          | 0.77               | 12987                        | 0.77               | 12987                        |
| 100000         | 4.76               | 21008                        | 4.76               | 21008                        |
| 1000000        | 70.3               | 14225                        | 70.3               | 14225                        |

<h4>3. Redis (AOF, fsync = no)</h4>

```
appendonly yes
appendfsync no
```
| Messages (qty) | Publish time (sec) | Publish throughput (msg/sec) | Consume time (sec) | Consume throughput (msg/sec) |
|----------------|--------------------|------------------------------|--------------------|------------------------------|
| 10000          | 0.75               | 13333                        | 0.75     b         | 13333                        |
| 100000         | 4.75               | 21052                        | 4.75               | 21052                        |
| 1000000        | 67.7               | 14771                        | 67.7               | 14771                        |

<h4>4. Redis (RDB)</h4>

```
appendonly no
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
```
| Messages (qty) | Publish time (sec) | Publish throughput (msg/sec) | Consume time (sec) | Consume throughput (msg/sec) |
|----------------|--------------------|------------------------------|--------------------|------------------------------|
| 10000          | 0.75               | 13333                        | 0.75     b         | 13333                        |
| 100000         | 4.75               | 21052                        | 4.75               | 21052                        |
| 1000000        | 68.7               | 14556                        | 68.7               | 14556                        |

<h4>5. Redis FIFO queue</h4>

Enqueue and dequeue
```
POST http://localhost:8080/api/redis/enqueue-async?qty=1000000
POST http://localhost:8080/api/redis/dequeue?notifyQty=1000000
```
| Messages (qty) | Publish time (sec) | Publish throughput (msg/sec) | Consume time (sec) | Consume throughput (msg/sec) |
|----------------|--------------------|------------------------------|--------------------|------------------------------|
| 10000          | 0.39               | 25641                        | 0.33               | 30303                        |
| 100000         | 4.40               | 22727                        | 3.16               | 31645                        |
| 1000000        | 36                 | 27777                        | 34.5               | 28985                        |

Conclusions:

1. Beanstalkd shows best results on pushing event to queue, worst on reading (maybe because of 2 operation on read: reserve and delete).
2. Redis AOF (fsync = everysec) FIFO queue performance seems better than Pub/Sub.
3. Redis AOF (fsync = no,everysec) and RDB demonstrate similar results for Pub/Sub.