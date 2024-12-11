#!/bin/sh

# Replace environment variables in the configuration file
sed -i "s/\$SENTINEL_QUORUM/$SENTINEL_QUORUM/g" /etc/redis/sentinel.conf
sed -i "s/\$SENTINEL_DOWN_AFTER/$SENTINEL_DOWN_AFTER/g" /etc/redis/sentinel.conf
sed -i "s/\$SENTINEL_FAILOVER/$SENTINEL_FAILOVER/g" /etc/redis/sentinel.conf


chown -R redis:redis /etc/redis
# Run the Redis Sentinel with the updated configuration
exec docker-entrypoint.sh redis-server /etc/redis/sentinel.conf --sentinel