package com.netty.rpc;

import com.netty.rpc.base.BaseTest;
import com.netty.rpc.consumer.RpcProxy;
import com.netty.rpc.service.HelloService;
import org.junit.Assert;
import org.testng.annotations.Test;
import javax.annotation.Resource;

/**
 * 消费者启动类
 */
public class HelloServiceTest extends BaseTest {
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
