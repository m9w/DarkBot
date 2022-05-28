package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.extensions.util.Version;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.google.gson.JsonParser;
import eu.darkbot.fabio.api.ManageAPI;
import eu.darkbot.fabio.api.SchifoAPI;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "BotUpdater", description = "This module auto update Darkbot")
public class BotUpdater implements Task, Configurable<BotUpdater.UpdaterConfig> {
    private BotUpdater.UpdaterConfig updaterConfig;
    public void install(Main main) {
        ManageAPI.init();
        try {
            Version lastVersion = new Version(BotUpdater.getVersion("version"));
            if (Main.VERSION.compareTo(lastVersion) < 0) {
                JButton cancel = new JButton("Cancel");
                JButton download = new JButton("Update");
                Popups.showMessageAsync("BotUpdater", new JOptionPane("A new version of Darkbot is available\n" + Main.VERSION + " ➜ " + lastVersion, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{download, cancel}, download));
                cancel.addActionListener((event) -> SwingUtilities.getWindowAncestor(cancel).setVisible(false));
                download.addActionListener((event) -> this.updateLogic(main, lastVersion));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVersion(String param) throws IOException {
        URLConnection request = new URL("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/version.json").openConnection();
        request.connect();
        return new JsonParser().parse(new InputStreamReader((InputStream)request.getContent())).getAsJsonObject().get(param).getAsString();
    }

    public void tick() {}

    private void updateLogic(Main main, Version newVersion) {
        main.featureRegistry.getFeatureDefinition(this).getIssues().addWarning(Main.VERSION.toString(), newVersion.toString());
        try (BufferedInputStream in = new BufferedInputStream((new URL(updaterConfig.source)).openStream())) {
            try (FileOutputStream out = new FileOutputStream("DarkBot.jar")) {
                byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) out.write(data, 0, count);

                if (this.updaterConfig.useOwnFile) {
                    if (this.updaterConfig.autoHideApi) {
                        if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties -hide");
                        else Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties -hide");
                    } else if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties");
                    else Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties");
                } else if (this.updaterConfig.autoHideApi) {
                    if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar -hide");
                    else Runtime.getRuntime().exec("javaw -jar DarkBot.jar -hide");
                } else if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar");
                else Runtime.getRuntime().exec("javaw -jar DarkBot.jar");

                System.exit(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConfig(BotUpdater.UpdaterConfig updaterConfig) {
        this.updaterConfig = updaterConfig;
    }

    public static class UpdaterConfig {
        @Option(value = "Use file.properties", description = "Use file.properties for auto login")
        public boolean useOwnFile = false;
        @Option(value = "Auto hide API", description = "After auto restart the API will auto hide")
        public boolean autoHideApi = false;
        @Option(value = "Source for download")
        public String source = "https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/DarkBot.jar";
    }
}
