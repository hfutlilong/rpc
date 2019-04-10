package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现
 */
@Service
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private volatile List<String> dataList = new ArrayList<>();

    /* 注册地址 */
    @Value("${registry.address}")
    private String registryAddress;

    @Resource
    private ZkService zkService;

//    /**
//     * 监视zk节点
//     *
//     * @param zk
//     */
//    private void watchNode() {
//
//        try {
//            List<String> nodeList = zk.getChildren(Constant.ZkConstant.SERVICE_ROOT_PATH, new Watcher() {
//                @Override
//                public void process(WatchedEvent watchedEvent) {
//                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
//                        watchNode(zk);
//                    }
//                }
//            });
//
//            List<String> dataList = new ArrayList<>();
//            byte[] bytes;
//            for (String node : nodeList) {
//                bytes = zk.getData(Constant.ZkConstant.SERVICE_ROOT_PATH + "/" + node, false, null);
//                dataList.add(new String(bytes));
//            }
//            LOGGER.debug("node data: {}", dataList);
//            this.dataList = dataList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOGGER.error("监视zk节点异常...", e);
//        }
//    }

    /**
     * 服务发现
     *
     * @return
     */
    public String discovery() {
        dataList = zkService.getDataFromCache();

        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                // 若只有一个地址，则获取该地址
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                // 若存在多个地址，则随机获取一个地址
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
    }
}
