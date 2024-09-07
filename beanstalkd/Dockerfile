FROM alpine

RUN apk add --no-cache beanstalkd

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh

VOLUME [ "/var/cache/beanstalkd" ]

EXPOSE 11300
ENTRYPOINT ["/entrypoint.sh"]
CMD ["beanstalkd"]