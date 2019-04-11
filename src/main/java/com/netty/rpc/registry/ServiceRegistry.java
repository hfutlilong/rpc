package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.Set;

/**
 * 服务注册
 */
public class ServiceRegistry {

    /* 注册地址 */
    private String registryAddress;

    private CuratorFramework client;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(Set<String> serviceNames, String serverAddress) throws Exception {
        if (CollectionUtils.isEmpty(serviceNames)) {
            return;
        }

        connectZkServer();

        for (String serviceName : serviceNames) {
            // 创建的临时节点示例：/registry/xxx.service/providers/data_0001
            String dataPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                    + Constant.ZkConstant.ZK_SEPERATOR + serviceName + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PROVIDERS_PATH + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PATH_PREFIX;
            /* 创建zk节点 */
            createZkNode(dataPath, serverAddress);
        }
    }

    private void connectZkServer() {
        client = CuratorFrameworkFactory.newClient(registryAddress,
                new ExponentialBackoffRetry(Constant.ZkConstant.BASE_SLEEP_TIME_MS, Constant.ZkConstant.MAX_RETRIES));
        client.start();

        addJvmHook();
    }

    /**
     * 创建zk临时顺序节点
     *
     * @param path
     * @param data
     * @throws Exception
     */
    public void createZkNode(String path, String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            client.create().creatingParentContainersIfNeeded().withProtection()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
        } else {
            byte[] bytes = data.getBytes();
            client.create().creatingParentContainersIfNeeded().withProtection()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, bytes);
        }
    }

    /**
     * 注册钩子
     */
    private void addJvmHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (client != null) {
                client.close();
            }
        }));
    }
}
