package co.paralleluniverse.fibers.redis.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import co.paralleluniverse.fibers.FiberUtil;
import co.paralleluniverse.fibers.redis.HostAndPortUtil;
import redis.clients.jedis.*;

public class HashingBenchmark {
    private static HostAndPort hnp1 = HostAndPortUtil.getRedisServers().get(0);
    private static HostAndPort hnp2 = HostAndPortUtil.getRedisServers().get(1);
    private static final int TOTAL_OPERATIONS = 100000;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        FiberUtil.runInFiber(() -> {
            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            JedisShardInfo shard = new JedisShardInfo(hnp1.getHost(), hnp1.getPort());
            shard.setPassword("foobared");
            shards.add(shard);
            shard = new JedisShardInfo(hnp2.getHost(), hnp2.getPort());
            shard.setPassword("foobared");
            shards.add(shard);
            ShardedJedis jedis = new ShardedJedis(shards);
            Collection<Jedis> allShards = jedis.getAllShards();
            allShards.forEach(BinaryJedis::flushAll);

            long begin = Calendar.getInstance().getTimeInMillis();

            for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
                String key = "foo" + n;
                jedis.set(key, "bar" + n);
                jedis.get(key);
            }

            long elapsed = Calendar.getInstance().getTimeInMillis() - begin;

            jedis.disconnect();

            System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops");
        });
    }
}