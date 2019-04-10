package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册
 */
public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    /*注册地址*/
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
            String watchPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                    + Constant.ZkConstant.ZK_SEPERATOR + serviceName + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PROVIDERS_PATH;

            // 创建的临时节点示例：/registry/xxx.service/providers/data_0001
            String dataPath = watchPath + Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.ZK_PATH_PREFIX;
            /* 创建zk节点 */
            createZkNode(dataPath, serverAddress);
//
//            /* 注册监听 */
//            watchZkNode(watchPath);
        }
    }


    private void connectZkServer() {
        client = CuratorFrameworkFactory.newClient(registryAddress,
                new ExponentialBackoffRetry(Constant.ZkConstant.BASE_SLEEP_TIME_MS,
                        Constant.ZkConstant.MAX_RETRIES));
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

//            if (cache != null) {
//                try {
//                    cache.close();
//                } catch (IOException e) {
//                    LOGGER.warn("close PathChildrenCache failed:{}.", e.getMessage(), e);
//                }
//            }

        }));
    }
}
