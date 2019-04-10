package com.netty.rpc.constant;

/**
 * 常量定义
 */
public class Constant {
    public static final int ZK_SESSION_TIMEOUT = 12000;
    public static final String ZK_REGISTRY_ROOT_PATH = "/registry";
    public static final String ZK_DATA_PATH = ZK_REGISTRY_ROOT_PATH + "/data";

    /**
     * 服务提供者端口号
     */
    public static final int PROVIDER_PORT = 8888;
}
