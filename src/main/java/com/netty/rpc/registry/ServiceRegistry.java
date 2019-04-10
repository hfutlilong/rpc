package com.netty.rpc.registry;

import com.netty.rpc.constant.Constant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 服务注册
 */
@Service
public class ServiceRegistry {
    @Resource
    private ZkService zkService;

    /**
     * 注册服务
     *
     * @param serviceNames
     * @param host
     * @param port
     */
    public void register(Set<String> serviceNames, String host, int port) throws Exception {
        if (CollectionUtils.isEmpty(serviceNames)) {
            return;
        }
        String serverAddress = host + ":" + port;

        for (String serviceName : serviceNames) {
            String watchPath = Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.SERVICE_ROOT_PATH
                    + Constant.ZkConstant.ZK_SEPERATOR + serviceName + Constant.ZkConstant.ZK_SEPERATOR
                    + Constant.ZkConstant.ZK_PROVIDERS_PATH;

            // 创建的临时节点示例：/registry/xxx.service/providers/data_0001
            String dataPath = watchPath + Constant.ZkConstant.ZK_SEPERATOR + Constant.ZkConstant.ZK_PATH_PREFIX;
            /* 创建zk节点 */
            zkService.createZkNode(dataPath, serverAddress);

            /* 注册监听 */
            zkService.watchZkNode(watchPath);
        }
    }
}
