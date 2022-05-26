package org.example.client;

import org.example.common.ControllerRegistry;
import java.io.IOException;
import java.util.List;

public class ClientService {
    public ServerPanelController serverPanelController;
    public SecondaryController secondaryController;
    public LocalPanelController localPanelController;
    public void loginOk() {
        try {
            App.setRoot("secondary");
            this.serverPanelController =
                    (ServerPanelController) ControllerRegistry.getControllerObject(ServerPanelController.class);
            this.secondaryController =
                    (SecondaryController) ControllerRegistry.getControllerObject(SecondaryController.class);
            this.localPanelController =
                    (LocalPanelController) ControllerRegistry.getControllerObject(LocalPanelController.class);
        } catch (IOException e) {
            System.out.printf("Ошибка при вызове второй сцены:" + e);
            throw new RuntimeException(e);
        }

    }
    public void putServerFilesList(String dir, List<ServerDirInfo> serverDirInfoList) {
        serverPanelController.updateServerFilesList(dir, serverDirInfoList);
    }

}
