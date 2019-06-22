package main.test;

import main.com.aa.ConsolidatedEquityBook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsolidatedEquityBookTest {

    @Test
    public void testClassExists(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();
        Assertions.assertNotNull(ceb);
    }
}
