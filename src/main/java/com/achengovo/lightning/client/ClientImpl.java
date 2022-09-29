package com.achengovo.lightning.client;

import com.achengovo.lightning.client.session.Session;
import com.achengovo.lightning.commons.code.Decode;
import com.achengovo.lightning.commons.code.Encode;
import com.achengovo.lightning.commons.message.RpcRequest;
import com.achengovo.lightning.commons.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 客户端
 */
public class ClientImpl implements Client {
    //服务端地址
    private String host;
    //服务端端口
    private int port;
    //权重
    private int weight;
    //是否可用
    private volatile boolean isAvailable;
    //session用于缓存RPC调用结果
    private Session session = new Session();
    //保存channel
    private Channel channel;
    //自增长id
    private AtomicLong atomicLong = new AtomicLong();

    /**
     * 构造函数
     * @param host 服务器地址
     * @param port 服务器端口
     * @param weight 权重
     */
    public ClientImpl(String host, int port,int weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.isAvailable = true;
    }

    /**
     * 获取端口
     * @return
     */
    public int getPort(){
        return port;
    }

    /**
     * 获取服务端地址
     * @return
     */
    public String getHost(){
        return host;
    }

    /**
     * 获取权重
     * @return
     */
    public int getWeight(){
        return weight;
    }

    /**
     * 是否可用
     * @return
     */
    public boolean isAvailable(){
        return isAvailable;
    }

    /**
     * 连接
     */
    public void connect() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(102400, 4, 4));
                        pipeline.addLast(new Encode());
                        pipeline.addLast(new Decode());
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if(msg instanceof RpcResponse){
                                    RpcResponse response = (RpcResponse) msg;
                                    //将结果放入session中
                                    session.addResult(response.getId(), response.getResult());
                                }
                                if(msg instanceof String){
                                    //接收到服务端发送的关闭信号
                                    if("close".equals(msg)){
                                        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);
                                        //设置客户端为不可用
                                        isAvailable = false;
                                        //检查是否有未完成的请求，如果没有，则关闭连接
                                        Thread thread=new Thread(()->{
                                            if(session.getSize()==0){
                                                ctx.channel().close();
                                                threadPool.shutdown();
                                            }
                                        });
                                        //定时任务，每隔一秒检查一次session中是否有任务未完成
                                        threadPool.scheduleAtFixedRate(thread,0,1000,TimeUnit.MILLISECONDS);
                                    }
                                }
                            }
                        });
                    }
                });
        channel = bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
    }

    /**
     * 发送请求
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Object request(RpcRequest request) throws ExecutionException, InterruptedException {
        request.setRequestId(atomicLong.incrementAndGet());
        CompletableFuture<Object> future = new CompletableFuture<>();
        session.addSession(request.getRequestId(), future);
        channel.writeAndFlush(request);
        return future.get();
    }
}
