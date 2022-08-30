package com.achengovo.lightning.commons.code;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectOutputStream;

/**
 * 编码器
 */
public class Encode extends MessageToByteEncoder<Object> {
    /**
     * 编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //写入魔数
        out.writeBytes(new byte[]{'a', 'b', 'c', 'd'});
        //记录writeIndex的位置
        int index = out.writerIndex();
        //移动writeIndex的位置,空出4个字节,用来写入消息长度
        out.writerIndex(index + 4);

        ByteBufOutputStream bos = new ByteBufOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        //写入消息
        oos.writeObject(msg);
        oos.flush();
        oos.close();
        bos.close();

        //写入消息的长度
        out.setInt(index, out.writerIndex() - index - 4);
    }
}
