package com.netty.rpc;

import com.alibaba.fastjson.JSON;
import com.netty.rpc.consumer.RpcProxy;
import com.netty.rpc.entity.SoaVO;
import com.netty.rpc.service.HelloService;
import com.netty.rpc.service.ThankYouService;
import com.netty.rpc.soa.SoaService;
import com.netty.rpc.utils.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 消费者启动类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:consumer/dubbo-consumer.xml"})
public class HelloServiceTest {
    @Resource
    private RpcProxy rpcProxy;

    @Resource
    private SoaService soaService;

    @Test
    public void helloTest(){
//        HelloService helloService = rpcProxy.create(HelloService.class);
//        String result = helloService.hello("world");
//        System.out.println("============>result: " + result);
//        Assert.assertEquals("Hello! world", result);
//
//        ThankYouService thankYouService = rpcProxy.create(ThankYouService.class);
//        result = thankYouService.thankYou("猴赛雷");
//        System.out.println("============>result: " + result);
//        Assert.assertEquals("Thank you, 猴赛雷", result);
//
//        Map<String, SoaVO> soaVOMap = soaService.getAllServices();
//        System.out.println("####### SOA：" + JSON.toJSONString(soaVOMap));
//
//        try {
//            Thread.sleep(5 * 60 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        Map<String, SoaVO> soaVOMapBefore = soaService.getAllServices();
        System.out.println("####### 事前SOA：" + JSON.toJSONString(soaVOMapBefore));

        CuratorFramework client = ZkUtil.connectZkServer("192.168.160.128:2181");
        client.start();
        try {
            ZkUtil.deleteZkNode(client, "/registry/com.netty.rpc.service.HelloService/providers/_c_23a8d78a-0066-4700-855a-0e0930bd1106-data0000000037");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, SoaVO> soaVOMapAfter = soaService.getAllServices();
        System.out.println("####### 事后SOA：" + JSON.toJSONString(soaVOMapAfter));

        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
