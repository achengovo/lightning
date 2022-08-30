package com.achengovo.lightning.commons.code;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ObjectInputStream;
import java.util.List;

/**
 * 解码器
 */
public class Decode extends ByteToMessageDecoder {
    /**
     * 解码
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //读取魔数
        byte[] magic = new byte[4];
        in.readBytes(magic, 0, 4);
        byte[] magicValue = new byte[]{'a', 'b', 'c', 'd'};
        for (int i = 0; i < magic.length; i++) {
            if ((char) magic[i] == magicValue[i]) {
                continue;
            } else {
                //魔数不对，关闭连接
                ctx.close();
                return;
            }
        }
        //读取消息长度
        int length = in.readInt();

        ByteBufInputStream bis = new ByteBufInputStream(in);
        ObjectInputStream ois = new ObjectInputStream(bis);
        //读取消息
        Object msg = ois.readObject();
        out.add(msg);
        ois.close();
        bis.close();
    }
}
