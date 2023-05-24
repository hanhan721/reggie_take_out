import com.itheima.reggie.ReggieApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Set;

@SpringBootTest(classes = ReggieApplication.class)
public class test {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public  void test() {
        System.out.println(redisTemplate.keys("dish*_*"));
    }
}
