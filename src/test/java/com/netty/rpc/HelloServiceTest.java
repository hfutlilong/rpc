package com.netty.rpc;

import com.netty.rpc.client.proxy.RpcProxy;
import com.netty.rpc.service.HelloService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 消费者启动类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:consumer/application-client.xml"})
public class HelloServiceTest {
    @Resource
    private RpcProxy rpcProxy;

    @Test
    public void helloTest(){
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("world");
        System.out.println("============>result: " + result);
        Assert.assertEquals("Hello! world", result);
    }
}
