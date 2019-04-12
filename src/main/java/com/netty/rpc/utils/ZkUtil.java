package com.netty.rpc.utils;

import com.netty.rpc.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @Description Zookeeper工具类
 * @Author lilong
 * @Date 2019-04-11 19:28
 */
public class ZkUtil {
    /**
     * 建立连接
     *
     * @param registryAddress
     * @return
     */
    public static CuratorFramework connectZkServer(String registryAddress) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddress,
                new ExponentialBackoffRetry(Constant.ZkConstant.BASE_SLEEP_TIME_MS, Constant.ZkConstant.MAX_RETRIES));
        client.start();
        return client;
    }

    /**
     * 创建zk临时顺序节点
     *
     * @param path zk节点路径
     * @param data zk节点保存的内容
     * @throws Exception
     */
    public static void createZkNode(CuratorFramework client, String path, String data) throws Exception {
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
     *  删除一个节点
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deleteZkNode(CuratorFramework client, String path) throws Exception {
        client.delete().forPath(path);
    }

    /**
     * 删除一个节点，并递归删除所有子节点
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deleteZkNodeWithChildren(CuratorFramework client, String path) throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }

    /**
     * 注册钩子
     */
    public static void addJvmHook(CuratorFramework client) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (client != null) {
                client.close();
            }
        }));
    }
}
