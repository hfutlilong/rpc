package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现
 */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    /* 保存服务提供者地址 */
    private volatile Map<String, List<String>> serviceProviderMap = new ConcurrentHashMap<>();

    /* 注册地址 */
    private String registryAddress;

    /* 消费哪些服务 */
    private String consumerServices;

    private CuratorFramework client;

    public ServiceDiscovery(String registryAddress, String consumerServices) throws Exception {
        this.registryAddress = registryAddress;
        this.consumerServices = consumerServices;

        /* 连接zk服务 */
        connectZkServer();

        /* 监听zk服务 */
        watchZkNode();

        addJvmHook();
    }

    /**
     * 服务发现
     * 
     * @return
     */
    public String discovery(String service) {
        List<String> providers = serviceProviderMap.get(service);

        int size = providers.size();

        if (size == 0) {
            return null;
        } else if (size == 1) {
            return providers.get(0);
        } else {
            // 多个提供者，随机选择一个
            return providers.get(ThreadLocalRandom.current().nextInt(size));
        }
    }

    private void connectZkServer() {
        client = CuratorFrameworkFactory.newClient(registryAddress,
                new ExponentialBackoffRetry(Constant.ZkConstant.BASE_SLEEP_TIME_MS, Constant.ZkConstant.MAX_RETRIES));
        client.start();
    }

    /**
     * 监视zk节点
     */
    private void watchZkNode() throws Exception {
        if (StringUtils.isBlank(consumerServices)) {
            return;
        }

        String[] services = consumerServices.split(";");
        for (int i = 0; i < services.length; i++) {
            String service = services[i];

            // /registry/a.b.c.Service/providers
            String basePath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                    + Constant.ZkConstant.ZK_SEPERATOR + service + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PROVIDERS_PATH;

            List<String> providers = getChildrenContent(service, basePath);
            serviceProviderMap.put(service, providers);
        }
    }

    /**
     * 获取路径path下的所有子节点的内容
     * 
     * @param path
     * @return
     * @throws Exception
     */
    public List<String> getChildrenContent(String service, String path) throws Exception {

        if (client.checkExists().forPath(path) == null) {
            LOGGER.debug("path {} not exists.", path);
            return new ArrayList<>();
        }

        List<String> children = client.getChildren().usingWatcher(new ZKWatcher(service, path)).forPath(path);
        if (children == null || children.size() == 0) {
            LOGGER.debug("path {} has no children", path);
            return new ArrayList<>();
        }

        return getZkNodesContent(path, children);
    }

    /**
     * zookeeper监听节点数据变化
     * 
     * @author lizhiyang
     *
     */
    private class ZKWatcher implements CuratorWatcher {
        private String service;

        private String path;

        public ZKWatcher(String service, String path) {
            this.service = service;
            this.path = path;
        }

        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Event.EventType.NodeChildrenChanged) { // 监听子节点的变化
                List<String> children = client.getChildren().usingWatcher(new ZKWatcher(service, path)).forPath(path);
                serviceProviderMap.put(service, getZkNodesContent(path, children));
            }
        }
    }

    /**
     * 获取zk节点的内容
     * 
     * @param path
     * @param children
     * @return
     * @throws Exception
     */
    private List<String> getZkNodesContent(String path, List<String> children) throws Exception {
        List<String> childrenContent = new ArrayList<>();

        if (children != null && children.size() > 0) {
            for (String child : children) {
                String childPath = path + Constant.ZkConstant.ZK_SEPERATOR + child;
                byte[] b = client.getData().forPath(childPath);
                String value = new String(b, StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(value)) {
                    childrenContent.add(value);
                }
            }
        }

        return childrenContent;
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
