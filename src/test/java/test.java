import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class test {
    @Test
    public  void te() {
        BigDecimal n=new BigDecimal(123);
        BigDecimal n2=new BigDecimal(2);
        n.add(n2);
        System.out.println(n);
    }
}
