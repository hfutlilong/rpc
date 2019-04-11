package com.netty.rpc.entity;

import java.util.List;

/**
 * 服务治理详情
 */
public class SoaVO {
    /**
     * 服务
     */
    String service;

    /**
     * 提供者
     */
    List<String> providers;

    /**
     * 消费者
     */
    List<String> consumers;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

    public List<String> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<String> consumers) {
        this.consumers = consumers;
    }
}
