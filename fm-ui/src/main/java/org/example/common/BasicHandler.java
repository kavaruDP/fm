package org.example.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.client.FileInfo;
import org.example.client.FileMessage;
import org.example.client.ServerDirInfo;
import org.example.common.dto.*;
import org.example.common.dto.AuthResponse;
import org.example.common.dto.GetFileListResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;
import static org.example.client.ServerPanelController.FULL_CLIENT_HOME_PATH;

public class BasicHandler extends ChannelInboundHandlerAdapter {
    private static final String FULL_SERVER_ROOT_DIR = "C:\\Java\\fm\\root-dir\\";
    private  String FULL_CLIENT_HOME_DIR;
    public String clientCurrentDir;
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
            clientPassword = authRequest.getPassword();
            //Пароль может быть пустым. При нахождении в БД пары логин-пароль возвращается соответствующий им никнейм
            //Это и будет означать что аутентификация пройдена.
            //Ситуация с несуществующим в БД логином отдельно не обрабатывается (будет отказ).
            String nickname = SQLHandler.getNicknameByLoginAndPassword(clientLogin,clientPassword);
            if (nickname != null) {
                //Проверяем что на сервере есть соответствующий клиенту каталог. В противном случае создаем новый.
                //Если при создании каталога ловим IOException, то продолжать работу с данным логином нельзя, - шлем отказ.
                if(!updateClientHomePath(clientLogin)) {
                    ctx.writeAndFlush(new AuthResponse("Сбой при создании домашнего каталога. Попробуйте другой логин"));
                } else {
                    AuthResponse authResponse = new AuthResponse("login ok");
                    authResponse.setClientHomeDir(clientLogin);
                    ctx.writeAndFlush(authResponse);
                    System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " отправлен AuthResponse(login ok).");
                }
            }
            else {
                ctx.writeAndFlush(new AuthResponse("Введен неверный пароль или указан несуществующий логин"));
            }
            return;
        }

        if (request instanceof RegRequest) {
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
                boolean isRegistrationOk = SQLHandler.registration(clientLogin,clientPassword,clientLogin);
                if (isRegistrationOk) {
                    ctx.writeAndFlush(new AuthResponse("Вы зарегистрированы"));
                }
                else {
                    ctx.writeAndFlush(new AuthResponse("Ошибка при регистрации."));
                }
            } else {
                ctx.writeAndFlush(new AuthResponse("Этот логин занят. Попробуйте еще раз."));
            }
            return;
        }

        if (request instanceof GetFileListRequest) {
            GetFileListRequest getFileListRequest = (GetFileListRequest) request;
            clientCurrentDir = getFileListRequest.getClientCurrentDir();
            System.out.println(dateFormat.format(date) + " " + clientName + ": получен GetFileListRequest указывающий на текущий каталог = " + clientCurrentDir);
            if(clientCurrentDir == null || clientCurrentDir.isEmpty() || clientCurrentDir.isBlank()){
                System.out.println(dateFormat.format(date) + ": клиентский каталог для " + clientLogin + ". Не определен.");
                return;
            }
            else {
                List<ServerDirInfo> serverDirInfoList;
                serverDirInfoList = list(Paths.get(FULL_SERVER_ROOT_DIR + clientCurrentDir))
                        .map(path -> path.toFile())
                        .map(file -> new ServerDirInfo(file))
                        .collect(Collectors.toList());
                System.out.println(dateFormat.format(date) + ": на адрес " + ctx.channel().remoteAddress() + " - отправлен GetFileListResponse содержащий:");
                serverDirInfoList.forEach(System.out::println);

                ctx.writeAndFlush(new GetFileListResponse(clientCurrentDir, serverDirInfoList));
            }
            return;
        }
        if (request instanceof GetFileRequest) {
            GetFileRequest getFileRequest = (GetFileRequest) request;
            String fileDestPath = getFileRequest.getDestFilepath();
            String fileSrcPath = getFileRequest.getSrcFilepath();
            String filename = getFileRequest.getFilename();
            String fullServerFileName = FULL_SERVER_ROOT_DIR + fileSrcPath + "\\" + filename;
            Path path = Paths.get(fullServerFileName);
            if ( Files.exists(path) && Files.size(path) <= 20000) {
                System.out.println(dateFormat.format(date) + " готовим к отправке файл " + fullServerFileName);
                FileMessage fileMessage = new FileMessage(path);
                ctx.writeAndFlush(new GetFileResponse(fileMessage,fileDestPath));
            } else {
                System.out.println("Указанный путь " + fullServerFileName + " не существует либо превышен размер файла");
            }
            return;
        }
        if (request instanceof PutFileRequest) {
            PutFileRequest putFileRequest = (PutFileRequest) request;
            String destPath = FULL_SERVER_ROOT_DIR + putFileRequest.getDestPath();
            FileMessage fm = putFileRequest.getFileMessage();
            System.out.println(destPath);
            Path pathTo = Paths.get(destPath + "\\" + fm.getFilename());
            try {
                System.out.println(dateFormat.format(date) + " получаем файл " + pathTo);
                Files.write(pathTo, fm.getData(), StandardOpenOption.CREATE);
                ctx.writeAndFlush(new PutFileResponse("file ok",putFileRequest.getDestPath()));
            } catch (IOException e) {
                System.out.println("Сбой при получении файла");
                ctx.writeAndFlush(new PutFileResponse("file bad", putFileRequest.getDestPath()));
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(dateFormat.format(date) + ": " + clientName + " отключился");
        cause.printStackTrace();
        ctx.close();
    }

    private boolean updateClientHomePath (String clientLogin) {
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
