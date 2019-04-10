package com.netty.rpc.constant;

/**
 * 常量定义
 */
public class Constant {
    public static final int ZK_SESSION_TIMEOUT = 12000;

    public static class ZkConstant {
        /**
         * ZK路径分隔符
         */
        public static final String ZK_SEPERATOR = "/";

        /**
         * 创建zk连接的重试间隔
         */
        public static final int BASE_SLEEP_TIME_MS = 100;

        /**
         * 创建zk连接的最大重试次数
         */
        public static final int MAX_RETRIES = 3;

        /**
         * 服务注册总父节点
         */
        public static final String SERVICE_ROOT_PATH = "registry";

        /**
         * 服务提供者总父节点
         */
        public static final String ZK_PROVIDERS_PATH = "providers";

        /**
         * 服务消费者总父节点
         */
        public static final String ZK_CONSUMERS_PATH = "consumers";

        /**
         * zk节点前缀
         */
        public static final String ZK_PATH_PREFIX = "data";
    }
}
