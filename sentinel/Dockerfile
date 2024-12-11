FROM redis:latest

EXPOSE 26379

ADD sentinel.conf /etc/redis/sentinel.conf

RUN mkdir -p /var/lib/redis /etc/redis && \
    chmod 777 /var/lib/redis /etc/redis && \
    chown redis:redis /etc/redis/sentinel.conf /var/lib/redis /etc/redis \

COPY sentinel-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/sentinel-entrypoint.sh

ENTRYPOINT ["sentinel-entrypoint.sh"]