package org.example.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.client.FileMessage;
import org.example.client.PrimaryController;
import org.example.client.ClientService;
import org.example.common.dto.*;
import org.example.common.dto.BasicResponse;
import org.example.common.dto.GetFileListResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Date date;
    private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private final ClientService clientService = new ClientService();
    private final PrimaryController primaryController =
            (PrimaryController) ControllerRegistry.getControllerObject(PrimaryController.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       date = new Date();
       super.channelActive(ctx);
        System.out.println(dateFormat.format(date) + ": клиентский канал активирован. Адрес сервера: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        date = new Date();
        BasicResponse response = (BasicResponse) msg;

        if (response instanceof AuthResponse) {
            AuthResponse authResponse = (AuthResponse) response;
            String responseText = authResponse.getResult();
            System.out.println(dateFormat.format(date) + ": получен AuthResponse. Домашний каталог = " + authResponse.getClientHomeDir());
            if ("login ok".equals(responseText)) {
                clientService.loginOk();
                ctx.writeAndFlush(new GetFileListRequest(authResponse.getClientHomeDir()));
                System.out.println(dateFormat.format(date) + " на адрес " + ctx.channel().remoteAddress() + " отправлен GetFileListRequest");
                return;
            }
            primaryController.setResultField("");
            primaryController.setResultField(responseText);
            return;
        }

        if (response instanceof RegResponse) {
            RegResponse regResponse = (RegResponse) response;
            primaryController.setResultField("");
            primaryController.setResultField(regResponse.getResult());
            return;
        }

       if (response instanceof GetFileListResponse) {
           GetFileListResponse getFileListResponse = (GetFileListResponse) response;
           System.out.println(dateFormat.format(date) + ": получен GetFileListResponse. CURRENT_USER_SERVER_DIR = " + getFileListResponse.getClientStringDir());
           clientService.serverPanelController.updateServerFilesList(getFileListResponse.getClientStringDir(), getFileListResponse.getItemList());
           return;
//            TODO выяснить в каком месте закрывать контекст канала и надо ли это делать.
//            Если в этом месте закрыть контекст, то при сборке сцены словим ClassCastException: class java.lang.String cannot be cast to class javafx.scene.paint.Color
            //ctx.close();
        }

        if (response instanceof GetFileResponse) {
            GetFileResponse getFileResponse = (GetFileResponse) response;
            System.out.println(dateFormat.format(date) + ": получен GetFileResponse. " );
            FileMessage fm = getFileResponse.getFileMessage();
            clientService.localPanelController.copyFromServerToLocal(fm, getFileResponse.getDestPath());
            return;
        }

        if (response instanceof PutFileResponse) {
            PutFileResponse putFileResponse = (PutFileResponse) response;
            System.out.println(dateFormat.format(date) + ": получен PutFileResponse. " );
            if ("file ok".equals(putFileResponse.getResult())) {
                ctx.writeAndFlush(new GetFileListRequest(putFileResponse.getDestPath()));
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(dateFormat.format(date) + " - на клиенте в момент обработки ClientHandler поймали исключение");
        cause.printStackTrace();
        ctx.close();
    }

}
