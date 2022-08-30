package com.achengovo.lightning.server;

import com.achengovo.lightning.commons.code.Decode;
import com.achengovo.lightning.commons.code.Encode;
import com.achengovo.lightning.commons.message.RpcRequest;
import com.achengovo.lightning.commons.message.RpcResponse;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

import static java.lang.Thread.sleep;
/**
 * 服务端
 */
public class Server {
    Logger log=org.slf4j.LoggerFactory.getLogger(Server.class);
    //端口号
    private int port;
    //保存channel
    Set<Channel> channelSet = Collections.synchronizedSet(new HashSet<>());
    //保存服务实例
    private Map<String, Object> serviceInstances;
    public Server(Map<String, Object> serviceInstances, int port) {
        this.serviceInstances = serviceInstances;
        this.port = port;
    }
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    NioEventLoopGroup workGroup = new NioEventLoopGroup();
    public void start() throws Exception {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                    ChannelPipeline pipeline = nioSocketChannel.pipeline();
                                    pipeline.addLast(new LengthFieldBasedFrameDecoder(102400, 4, 4));
                                    pipeline.addLast(new Decode());
                                    pipeline.addLast(new Encode());
                                    pipeline.addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            RpcRequest request = (RpcRequest) msg;
                                            Long requestId = request.getRequestId();
                                            String interfaceName = request.getInterfaceName();
                                            String methodName = request.getMethodName();
                                            Object[] parameters = request.getParameters();
                                            Class<?>[] parameterTypes = (Class<?>[]) request.getParameterTypes();
                                            Object instance = serviceInstances.get(interfaceName);
                                            Method method = instance.getClass().getMethod(methodName, parameterTypes);
                                            method.setAccessible(true);
                                            Object result = method.invoke(instance, parameters);
                                            RpcResponse response = new RpcResponse();
                                            response.setId(requestId);
                                            response.setResult(result);
                                            ctx.writeAndFlush(response);
                                        }
                                        @Override
                                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                            channelSet.add(ctx.channel());
                                            ctx.fireChannelRegistered();
                                        }
                                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                            channelSet.remove(ctx.channel());
                                            ctx.fireChannelUnregistered();
                                        }
                                    });
                                }
                            });
                    ChannelFuture future = bootstrap.bind(port);
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            }
        });
        serverThread.start();
    }

    /**
     * 停止服务
     * @param naming 注册中心
     * @param serviceName 服务名称
     * @param groupName 分组名
     * @param instance 服务实例
     * @throws Exception
     */
    public void stop(NamingService naming,String serviceName,String groupName,Instance instance) throws Exception {
        log.info("准备关闭服务器");
        //从注册中心删除
        naming.deregisterInstance(serviceName,groupName,instance);
        //向所有客户端发送关闭消息
        for(Channel channel : channelSet){
            channel.writeAndFlush("close");
        }
        //停止接收
        bossGroup.shutdownGracefully();
        //等待所有连接关闭
        while (true){
            sleep(1000);
            if(channelSet.size() == 0){
                log.info("关闭服务器");
                workGroup.shutdownGracefully();
                break;
            }
        }
    }
}
