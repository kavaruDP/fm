package org.example.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.example.FileInfo;
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
import static org.example.ServerPanelController.FULL_CLIENT_HOME_PATH;

public class BasicHandler extends ChannelInboundHandlerAdapter {
    private static final String FULL_SERVER_ROOT_DIR = "C:\\Java\\fm\\root-dir\\";
    private static String FULL_CLIENT_HOME_DIR;
    private String clientCurrentDir;
    private String clientName;
    private String clientLogin;
    private String clientPassword;
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
            clientLogin =  authRequest.getLogin();
            if (clientLogin.isEmpty() || clientLogin.isBlank()) {
                ctx.writeAndFlush(new AuthResponse("Вы не ввели логин"));
                return;
            }
            clientPassword = authRequest.getPassword();
            String nickname = SQLHandler.getNicknameByLoginAndPassword(clientLogin,clientPassword);
            if (nickname != null) {
                if(!updateClientAccount(clientLogin)) {
                    ctx.writeAndFlush(new AuthResponse("Отсутствует домашний каталог"));
                } else {
                    AuthResponse authResponse = new AuthResponse("login ok");
                    authResponse.setFullClientHomeDir(FULL_CLIENT_HOME_DIR);
                    ctx.writeAndFlush(authResponse);
                    System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login ok).FULL_CLIENT_HOME_DIR = " + FULL_CLIENT_HOME_DIR);
                }
            }
            else {
                ctx.writeAndFlush(new AuthResponse("Введен неверный пароль"));
            }

        }  else if (request instanceof RegRequest) {
            RegRequest regRequest = (RegRequest) request;
            clientLogin =  regRequest.getLogin();
            if (clientLogin.isEmpty() || clientLogin.isBlank()) {
                ctx.writeAndFlush(new RegResponse("Отсутствует логин. Регистрация невозможна."));
                return;
            }
            clientPassword = regRequest.getPassword();
            if (clientPassword.isEmpty() || clientPassword.isBlank()) {
                ctx.writeAndFlush(new RegResponse("Отсутствует пароль. Регистрация невозможна."));
                return;
            }
            String nickname = SQLHandler.getNicknameByLoginAndPassword(clientLogin,clientPassword);
            if (nickname == null) {
                Boolean isRegistrationOk = SQLHandler.registration(clientLogin,clientPassword,clientLogin);
                if (isRegistrationOk) {
                    ctx.writeAndFlush(new AuthResponse("Вы зарегистрированы"));
                }
                else {
                    ctx.writeAndFlush(new AuthResponse("Ошибка при регистрации."));
                }
            } else {
                ctx.writeAndFlush(new AuthResponse("Этот логин занят. Попробуйте еще раз."));
            }

        } else if (request instanceof GetFileListRequest) {
            GetFileListRequest getFileListRequest = (GetFileListRequest) request;
            clientCurrentDir = getFileListRequest.getClientCurrentDir();
            System.out.println(dateFormat.format(date) + " " + clientName + ": получен GetFileListRequest указывающий на текущий каталог = " + clientCurrentDir);
            if(clientCurrentDir.isEmpty() || clientCurrentDir.isBlank()){
                System.out.println(dateFormat.format(date) + ": проблема с каталогом для логина " + clientLogin + ". Вероятнее всего недостаточно прав для его создания.");
                return;
            } else {
                List<File> filesList = Files.list(Paths.get(clientCurrentDir))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
/*
                List<FileInfo> serverFileInfoList = serverFileList.stream()
                        .map(File::toPath)
                        .map(FileInfo::new)
                        .collect(Collectors.toList());
 */
                //TODO убрать строку ниже после проверки
                //BasicResponse basicResponse = new GetFileListResponse(clientCurrentDir, filesList);
                ctx.writeAndFlush(new GetFileListResponse(clientCurrentDir, filesList));
                System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " - отправлен GetFileListResponse");
            }
        } else if (request instanceof GetFileRequest) {
            GetFileRequest getFileRequest = (GetFileRequest) request;
            String filepath = getFileRequest.getFilepath();
            String filename = getFileRequest.getFilename();
            Path path = Paths.get(filepath, filename);
            if ( Files.exists(path) && Files.size(path) <= 20) {
                FileInfo fi = new FileInfo(path);
                ctx.writeAndFlush(fi);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(dateFormat.format(date) + ": " + clientName + " отключился");
        cause.printStackTrace();
        ctx.close();
    }

    private boolean updateClientAccount (String clientLogin) {
        FULL_CLIENT_HOME_DIR = FULL_SERVER_ROOT_DIR + clientLogin;
        FULL_CLIENT_HOME_PATH = Paths.get(FULL_CLIENT_HOME_DIR);
        if(!Files.exists(FULL_CLIENT_HOME_PATH)) {
            System.out.println(dateFormat.format(date) + ": каталог для логина " + clientLogin + " на сервере не найден. Создаем новый...");
            try {
                FULL_CLIENT_HOME_PATH = Files.createDirectory(FULL_CLIENT_HOME_PATH);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
