package com.netty.rpc.registry;

import com.alibaba.fastjson.JSON;
import com.netty.rpc.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description ZK通用服务
 * @Author lilong
 * @Date 2019-04-10 17:48
 */
@Service
public class ZkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkService.class);

    /* 注册地址 */
    @Value("${registry.address}")
    private String registryAddress;

    /* zk连接管理 */
    private CuratorFramework client;

    /* zk事件监听 */
    private PathChildrenCache cache;

    /**
     * 连接zk
     */
    @PostConstruct
    public void init() {
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(Constant.ZkConstant.BASE_SLEEP_TIME_MS,
                Constant.ZkConstant.MAX_RETRIES);

        client = CuratorFrameworkFactory.newClient(registryAddress, retryPolicy);
        client.start();

        this.addJvmHook();
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
     * 监听zk路径
     * 
     * @param path
     * @throws Exception
     */
    public void watchZkNode(String path) throws Exception {
        cache = new PathChildrenCache(client, path, true);
        cache.start();

//        PathChildrenCacheListener cacheListener = (client, event) -> {
//            System.out.println("事件类型：" + event.getType());
//            if (null != event.getData()) {
//                System.out.println("节点数据：" + event.getData().getPath() + " = " + new String(event.getData().getData()));
//            }
//        };
//        cache.getListenable().addListener(cacheListener);
    }


    public List<String> getDataFromCache() {
        List<String> providerList = new ArrayList<>();

        if (cache.getCurrentData().size() == 0) {
            System.out.println("* empty *");
        } else {
            for (ChildData data : cache.getCurrentData()) {
                providerList.add(new String(data.getData()));
                System.out.println(data.getPath() + " = " + new String(data.getData()));
            }
        }

        LOGGER.info("########## providerList = {}", JSON.toJSONString(providerList));
        return providerList;
    }

    /**
     * 注册钩子
     */
    private void addJvmHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (client != null) {
                client.close();
            }

            if (cache != null) {
                try {
                    cache.close();
                } catch (IOException e) {
                    LOGGER.warn("close PathChildrenCache failed:{}.", e.getMessage(), e);
                }
            }

        }));
    }
}
