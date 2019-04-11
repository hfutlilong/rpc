package com.netty.rpc.soa;

import com.netty.rpc.entity.SoaVO;
import com.netty.rpc.registry.ServiceDiscovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoaService {
    private ServiceDiscovery serviceDiscovery;

    public SoaService(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public Map<String, SoaVO> getAllServices() {
        Map<String, List<String>> providers = serviceDiscovery.getServiceProviderMap();
        Map<String, List<String>> consumers = serviceDiscovery.getServiceConsumerMap();

        Map<String, SoaVO> allServicesMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : providers.entrySet()) {
            SoaVO soaVO = new SoaVO();
            soaVO.setService(entry.getKey());
            soaVO.setProviders(entry.getValue());
            allServicesMap.put(entry.getKey(), soaVO);
        }

        for (Map.Entry<String, List<String>> entry : consumers.entrySet()) {
            String service = entry.getKey();
            SoaVO soaVO = new SoaVO();
            if (allServicesMap.containsKey(service)) {
                soaVO = allServicesMap.get(service);
            }
            soaVO.setService(entry.getKey());
            soaVO.setConsumers(entry.getValue());
            allServicesMap.put(service, soaVO);
        }

        return allServicesMap;
    }
}
