package com.netty.rpc.service.impl;

import com.netty.rpc.annotation.RpcService;
import com.netty.rpc.service.ThankYouService;

/**
 * @Description TODO
 * @Author lilong
 * @Date 2019-04-12 11:59
 */
@RpcService
public class ThankYouServiceImpl implements ThankYouService {
    @Override
    public String thankYou(String name) {
        return "Thank you, " + name;
    }
}
