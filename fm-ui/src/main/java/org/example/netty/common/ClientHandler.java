package org.example.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.netty.client.Callback;
import org.example.netty.common.dto.BasicRequest;
import org.example.netty.common.dto.BasicResponse;
import org.example.netty.common.dto.Command;
import org.example.netty.common.dto.GetFileListRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


// Используется при обработке pipeline на стороне клиента (NettyClient)
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       super.channelActive(ctx);
    }
    //Для получения данных от сервера воспользуемся блокирующей очередью.
    //Она приостановит выполнение программы, пока в нее не будут добавлены новые данные
    //private final BlockingQueue<Command> answer = new LinkedBlockingQueue<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        BasicResponse response = (BasicResponse) msg;
        System.out.println(response.getResponse());
        String responseText = response.getResponse();

        if ("login bad".equals(responseText)) {
            return;
        }
        if ("file list....".equals(responseText)) {
            ctx.close();
            return;
        }
        ctx.writeAndFlush(new GetFileListRequest());
    }

}
