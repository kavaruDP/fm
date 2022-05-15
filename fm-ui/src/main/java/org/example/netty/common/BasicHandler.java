package org.example.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.netty.common.dto.*;
import org.example.netty.common.dto.AuthResponse;
import org.example.netty.common.dto.BasicResponse;
import org.example.netty.common.dto.GetFileListResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class BasicHandler extends ChannelInboundHandlerAdapter {
    private static final String ROOT_DIR = "C:\\Java\\fm\\root-dir\\";
    private String clientStringDir;
    private Path clientPathDir;
    private String clientName;
    private String clientLogin;
    private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private Date date;

    private static int newClientIndex = 1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        TODO возможно стоит убрать
        super.channelActive(ctx);
        date = new Date();
        clientName = "Клиент №" + newClientIndex;
        newClientIndex++;
        System.out.println(dateFormat.format(date) + " " + "Подключился " + clientName + " с адреса " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        date = new Date();
        BasicRequest request = (BasicRequest) msg;

        if (request instanceof AuthRequest) {
            System.out.println(dateFormat.format(date) + " " + clientName + ": получен AuthRequest");
            AuthRequest authRequest = (AuthRequest) request;
//            TODO написать метод для аутентификации по паролю
            if (authRequest.getPassword().equals("123")) {
                clientLogin =  authRequest.getLogin();
                if (clientLogin.isEmpty() || clientLogin.isBlank()) {
                    ctx.writeAndFlush(new AuthResponse("login bad"));
                    System.out.println(dateFormat.format(date) + ":отсутствует логин. На адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login bad)");
                    return;
                }
                if(!updateClientAccount(clientLogin)) {
                    ctx.writeAndFlush(new AuthResponse("login bad"));
                    System.out.println(dateFormat.format(date) + ": отсутствует домашний каталог. На адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login bad)");
                } else {
                    ctx.writeAndFlush(new AuthResponse("login ok"));
                    System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login ok)");
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Введен неверный пароль!", ButtonType.OK);
                alert.showAndWait();
                ctx.writeAndFlush(new AuthResponse("login bad"));
                System.out.println(dateFormat.format(date) + ": введен неверный пароль. На адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login bad)");
            }

        } else if (request instanceof GetFileListRequest) {
            System.out.println(dateFormat.format(date) + " " + clientName + ": получен GetFileListRequest");
            if(clientStringDir.isEmpty() || clientStringDir.isBlank() || clientPathDir == null){
                System.out.println(dateFormat.format(date) + ": проблема с каталогом для логина " + clientLogin + ". Вероятнее всего недостаточно прав для его создания.");
                return;
            } else {
                List<File> filesList = Files.list(clientPathDir)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                BasicResponse basicResponse = new GetFileListResponse(clientStringDir, filesList);
                ctx.writeAndFlush(basicResponse);
                System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " - отправлен GetFileListResponse");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(clientName + " отключился");
        cause.printStackTrace();
        ctx.close();
    }

    private boolean updateClientAccount (String clientLogin) {
        clientStringDir = ROOT_DIR + clientLogin;
        System.out.println(clientName + ": clientStringDir = " + clientStringDir);
        clientPathDir = Paths.get(clientStringDir);
        if(!Files.exists(clientPathDir)) {
            System.out.println(dateFormat.format(date) + ": каталог для логина " + clientLogin + " на сервере не найден. Создаем новый...");
            try {
                clientPathDir = Files.createDirectory(clientPathDir);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
