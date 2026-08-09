package com.pubsub.salesforcepubsubapispringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "salesforce-subscribe-config.event-listening-on=false")
class SalesforcePubSubApiSpringbootApplicationTests {

    @Test
    void contextLoads() {
    }

}
