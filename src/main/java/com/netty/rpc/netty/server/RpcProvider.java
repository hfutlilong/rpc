package com.netty.rpc.netty.server;

import com.netty.rpc.annotation.RpcService;
import com.netty.rpc.entity.RpcRequest;
import com.netty.rpc.entity.RpcResponse;
import com.netty.rpc.netty.codec.RpcDecoder;
import com.netty.rpc.netty.codec.RpcEncoder;
import com.netty.rpc.registry.ServiceRegistry;
import com.netty.rpc.utils.NetwokUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC服务器
 */
public class RpcProvider implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProvider.class);

    /* 服务注册中心 */
    private ServiceRegistry serviceRegistry;

    /**
     * 服务提供者端口号
     */
    private int servicePort;

    /* 存放接口名与服务对象之间的映射关系 */
    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcProvider(ServiceRegistry serviceRegistry, int servicePort) {
        this.serviceRegistry = serviceRegistry;
        this.servicePort = servicePort;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        /* 获取所有带@RpcService注解的Spring Bean */
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (null != serviceBeanMap && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建并初始化 Netty 服务端 Bootstrap 对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // 将RPC请求进行解码（为了处理请求）
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    // 将RPC请求进行编码（为了返回响应）
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    // 处理RPC请求
                                    .addLast(new RpcChannelHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            // 启动RPC服务端
            String host = NetwokUtils.getLocalhost(); // 服务地址

            ChannelFuture channelFuture = bootstrap.bind(host, servicePort).sync();
            LOGGER.debug("server started on port: {}", servicePort);

            if (null != serviceRegistry) {
                String serverAddress = host + ":" + servicePort;
                // 注册服务地址
                serviceRegistry.register(handlerMap.keySet(), serverAddress);
                LOGGER.debug("register service:{}", serverAddress);
            }

            // 关闭RPC服务器
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }
}
