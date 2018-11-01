package quota;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;

public class QuotaCheckerTest extends HazelcastTestSupport {

    @Test
    public void startServer() {
        int quotaInBytes = 500 * 1024;
        Config config = new Config();
        config.getGroupConfig().setName("checkQuota");
        config.setClientQuotaInBytes(quotaInBytes);
        Hazelcast.newHazelcastInstance(config);
        sleepSeconds(10000);

    }
}
