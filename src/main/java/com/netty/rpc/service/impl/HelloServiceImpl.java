package com.netty.rpc.service.impl;

import com.netty.rpc.annotation.RpcService;
import com.netty.rpc.service.HelloService;

/**
 * 实现服务接口
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}
