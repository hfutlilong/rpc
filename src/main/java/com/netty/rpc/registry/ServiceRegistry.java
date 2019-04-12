package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import com.netty.rpc.utils.ZkUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;

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

    public void registerProvider(Set<String> serviceNames, String serverAddress) throws Exception {
        if (CollectionUtils.isEmpty(serviceNames)) {
            return;
        }

        client = ZkUtil.connectZkServer(registryAddress);

        for (String serviceName : serviceNames) {
            // 创建的临时节点示例：/registry/xxx.service/providers/data_0001
            String dataPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                    + Constant.ZkConstant.ZK_SEPERATOR + serviceName + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PROVIDERS_PATH + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PATH_PREFIX;
            /* 创建zk节点 */
            ZkUtil.createZkNode(client, dataPath, serverAddress);

            // TODO 测试代码
            ZkUtil.createZkNode(client, dataPath, "1.2.3.4:8080");
            ZkUtil.createZkNode(client, dataPath, "2.3.4.5:8181");
            ZkUtil.createZkNode(client, dataPath, "3.4.5.6:8282");

        }

        ZkUtil.addJvmHook(client);
    }
}
