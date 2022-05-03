package org.example.netty.client;

import org.example.App;
import org.example.ServerPanelController;
import org.example.netty.common.ControllerRegistry;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ClientService {
    public void loginOk() {
        try {
            App.setRoot("secondary");
        } catch (IOException e) {
            System.out.printf("Ошибка при вызове второй сцены:" + e);
            throw new RuntimeException(e);
        }
    }

    public void putServerFileList(Path path, List<File> serverItemList) {
        ServerPanelController controllerObject =
                (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
        //controllerObject.renderServerFileList(serverItemList);
        controllerObject.updateServerFilesList(path, serverItemList);
    };

}
