package org.example.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.PrimaryController;
import org.example.ServerPanelController;
import org.example.netty.client.ClientService;
import org.example.netty.common.dto.*;
import org.example.netty.common.dto.BasicResponse;
import org.example.netty.common.dto.GetFileListResponse;

import java.io.File;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import static org.example.ServerPanelController.CURRENT_LEVEL_SERVER_DIR;

// Используется при обработке pipeline на стороне клиента (NettyClient)
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Date date;
    private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private final ClientService clientService = new ClientService();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       date = new Date();
       super.channelActive(ctx);
        System.out.println(dateFormat.format(date) + ": клиентский канал активирован. Адрес сервера: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        date = new Date();
        PrimaryController controllerObject =
                (PrimaryController) ControllerRegistry.getControllerObject(PrimaryController.class);
        BasicResponse response = (BasicResponse) msg;

        if (response instanceof AuthResponse) {
            AuthResponse authResponse = (AuthResponse) response;
            String responseText = authResponse.getResult();
            System.out.println(dateFormat.format(date) + ": получен AuthResponse. Домашний каталог = " + authResponse.getFullClientHomeDir());
            if ("login ok".equals(responseText)) {
                ctx.writeAndFlush(new GetFileListRequest(authResponse.getFullClientHomeDir()));
                System.out.println(dateFormat.format(date) + " на адрес " + ctx.channel().remoteAddress() + " отправлен GetFileListRequest");
                clientService.loginOk();
                return;
            }
            controllerObject.setResultField(responseText);
            return;
        }

        if (response instanceof RegResponse) {
            RegResponse regResponse = (RegResponse) response;

            controllerObject.setResultField(regResponse.getResult());
            return;
        }

       if (response instanceof GetFileListResponse) {
            GetFileListResponse getFileListResponse = (GetFileListResponse) response;
            System.out.println(dateFormat.format(date) + ": получен GetFileListResponse. CURRENT_USER_SERVER_DIR = " + getFileListResponse.getClientStringDir());
            List<File> serverItemList = ( (GetFileListResponse) response ).getItemList();
            clientService.putServerFileList(getFileListResponse.getClientStringDir(), serverItemList);
            return;

//            TODO выяснить в каком месте закрывать контекст канала и надо ли это делать.
//            Если в этом месте закрыть контекст, то при сборке сцены словим ClassCastException: class java.lang.String cannot be cast to class javafx.scene.paint.Color
            //ctx.close();
        }

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(dateFormat.format(date) + " - на клиенте в момент обработки ClientHandler поймали исключение");
        cause.printStackTrace();
        ctx.close();
    }

}
