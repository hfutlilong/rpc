package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import com.netty.rpc.utils.NetwokUtils;
import com.netty.rpc.utils.ZkUtil;
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

    /* 保存服务消费者地址 */
    private volatile Map<String, List<String>> serviceConsumerMap = new ConcurrentHashMap<>();

    /* 消费哪些服务 */
    private String consumerServices;

    private CuratorFramework client;

    public ServiceDiscovery(String registryAddress, String consumerServices) throws Exception {
        this.consumerServices = consumerServices;

        /* 连接zk服务 */
        client = ZkUtil.connectZkServer(registryAddress);

        /* 监听zk服务 */
        watchZkNode();

        ZkUtil.addJvmHook(client);
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

    /**
     * 监听服务节点，并在服务下面注册消费者，并监听消费者
     */
    private void watchZkNode() throws Exception {
        if (StringUtils.isBlank(consumerServices)) {
            return;
        }

        String host = NetwokUtils.getLocalhost(); // 获取本机地址

        String[] serviceNames = consumerServices.split(";");
        for (int i = 0; i < serviceNames.length; i++) {
            String serviceName = serviceNames[i];
            registerConsumer(serviceName, host); // 在服务目录注册消费者
            watchProducerNode(serviceName); // 监听生产者
            watchConsumerNode(serviceName); // 监听消费者
        }
    }

    /**
     * 注册消费者
     */
    private void registerConsumer(String serviceName, String host) throws Exception {
        // 创建的临时节点示例：/registry/xxx.service/providers/data_0001
        String dataPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                + Constant.ZkConstant.ZK_SEPERATOR + serviceName + Constant.ZkConstant.ZK_SEPERATOR
                + Constant.ZkConstant.ZK_CONSUMERS_PATH + Constant.ZkConstant.ZK_SEPERATOR
                + Constant.ZkConstant.ZK_PATH_PREFIX;
        /* 创建zk节点 */
        ZkUtil.createZkNode(client, dataPath, host);


        // TODO 测试代码
        ZkUtil.createZkNode(client, dataPath, "192.168.0.1");
        ZkUtil.createZkNode(client, dataPath, "192.168.0.2");
        ZkUtil.createZkNode(client, dataPath, "192.168.0.3");
        ZkUtil.createZkNode(client, dataPath, "192.168.0.4");
    }

    /**
     * 监听服务提供者
     * 
     * @param service
     * @throws Exception
     */
    private void watchProducerNode(String service) throws Exception {
        // /registry/a.b.c.Service/providers
        String providerPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                + Constant.ZkConstant.ZK_SEPERATOR + service + Constant.ZkConstant.ZK_SEPERATOR
                + Constant.ZkConstant.ZK_PROVIDERS_PATH;
        // 监听服务节点
        List<String> providers = getChildrenContent(service, providerPath, NodeTypeEnum.PROVIDER);
        serviceProviderMap.put(service, providers);
    }

    /**
     * 监听服务消费者
     * 
     * @param service
     * @throws Exception
     */
    private void watchConsumerNode(String service) throws Exception {
        // /registry/a.b.c.Service/consumers
        String consumerPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                + Constant.ZkConstant.ZK_SEPERATOR + service + Constant.ZkConstant.ZK_SEPERATOR
                + Constant.ZkConstant.ZK_CONSUMERS_PATH;

        // 监听消费者
        List<String> consumers = getChildrenContent(service, consumerPath, NodeTypeEnum.CONSUMER);
        serviceConsumerMap.put(service, consumers);
    }

    /**
     * 获取路径path下的所有子节点的内容
     * 
     * @param path
     * @return
     * @throws Exception
     */
    public List<String> getChildrenContent(String service, String path, NodeTypeEnum nodeType) throws Exception {

        if (client.checkExists().forPath(path) == null) {
            LOGGER.debug("path {} not exists.", path);
            return new ArrayList<>();
        }

        List<String> children = client.getChildren().usingWatcher(new ZKWatcher(service, path, nodeType)).forPath(path);
        if (children == null || children.size() == 0) {
            LOGGER.debug("path {} has no children", path);
            return new ArrayList<>();
        }

        return getZkNodesContent(path, children);
    }

    /**
     * zookeeper监听节点数据变化
     */
    private class ZKWatcher implements CuratorWatcher {
        private String service;

        private String path;

        NodeTypeEnum nodeType;

        public ZKWatcher(String service, String path, NodeTypeEnum nodeType) {
            this.service = service;
            this.path = path;
            this.nodeType = nodeType;
        }

        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Event.EventType.NodeChildrenChanged) { // 监听子节点的变化
                List<String> children = client.getChildren().usingWatcher(new ZKWatcher(service, path, nodeType))
                        .forPath(path);

                switch (nodeType) {
                    case PROVIDER:
                        serviceProviderMap.put(service, getZkNodesContent(path, children));
                        break;
                    case CONSUMER:
                        serviceConsumerMap.put(service, getZkNodesContent(path, children));
                        break;
                    default:
                        // 节点类型要么是生产者、要么是消费者，不允许是其他类型
                        throw new RuntimeException("param nodeType error.");
                }
            }
        }
    }

    private enum NodeTypeEnum {
        PROVIDER, CONSUMER
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

    public Map<String, List<String>> getServiceProviderMap() {
        return serviceProviderMap;
    }

    public Map<String, List<String>> getServiceConsumerMap() {
        return serviceConsumerMap;
    }
}
