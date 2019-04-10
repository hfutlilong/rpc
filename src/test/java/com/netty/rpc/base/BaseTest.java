package com.netty.rpc.base;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration(locations = { "classpath:/test-config.xml" })
public class BaseTest extends AbstractTestNGSpringContextTests {

    public BaseTest() {
        super();
    }
}
