package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.RegisterFeature;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.ManageAPI;
import eu.darkbot.fabio.utils.ConfigsSupplier;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JComponent;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "AutoRestart", description = "Auto restart of Darkbot at time that you want")
public class AutoRestart implements Task, InstructionProvider, Configurable<AutoRestart.AutoStartConfig> {
    private Main main;
    private AutoRestart.AutoStartConfig autoStartConfig;

    public void install(Main main) {
        this.main = main;
        this.createFile();
        ManageAPI.init();
    }

    public void tickTask() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        if (this.ifFileNotEmpty() && this.autoStartConfig.Time.equals(formatter.format(date))) {
            try {
                if (this.autoStartConfig.autoHideApi) {
                    if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties -config " + this.autoStartConfig.configs + " -hide");
                    else Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties -config " + this.autoStartConfig.configs + " -hide");
                } else if (Main.VERSION.getBeta() >= 100) SchifoAPI.sendCommand("javaw -jar DarkBot.jar -start -login file.properties -config " + this.autoStartConfig.configs);
                else Runtime.getRuntime().exec("javaw -jar DarkBot.jar -start -login file.properties -config " + this.autoStartConfig.configs);
                if (this.autoStartConfig.disableInRestart) this.main.featureRegistry.getFeatureDefinition(this).setStatus(false);
                System.exit(20);
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }
    }

    public void setConfig(AutoRestart.AutoStartConfig autoStartConfig) {
        this.autoStartConfig = autoStartConfig;
    }

    public void tick() {}

    private void createFile() {
        File f = new File("file.properties");
        if (!f.exists()) {
            try (OutputStream outputStream = new FileOutputStream("file.properties")) {
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                Properties properties = new Properties();
                map.put("username", "");
                map.put("password", "");
                map.put("master_password", "");
                properties.putAll(map);
                properties.store(outputStream, "Write data after '='");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.ifFileNotEmpty();
    }

    private boolean ifFileNotEmpty() {
        try {
            Properties p = new Properties();
            FileReader reader = new FileReader("file.properties");
            p.load(reader);
            if (!p.getProperty("username").isEmpty() && !p.getProperty("password").isEmpty()) {
                if (!this.main.featureRegistry.getFeatureDefinition(this).getIssues().getIssues().isEmpty()) {
                    this.main.featureRegistry.getFeatureDefinition(this).getIssues().getIssues().clear();
                    this.main.pluginUpdater.checkUpdates();
                }
                return true;
            }
            this.main.featureRegistry.getFeatureDefinition(this).getIssues().addWarning("Warning: user data are empty, click on gear and open file then write credential", "");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JComponent beforeConfig() {
        JButton openFile = new JButton("Open 'file.properties' file");
        openFile.addActionListener((e) -> {
            if (Desktop.isDesktopSupported()) {
                File file = new File("file.properties");
                Desktop desktop = Desktop.getDesktop();
                try {
                    if (!file.exists()) this.createFile();
                    desktop.open(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        });
        return openFile;
    }

    public static class AutoStartConfig {
        @Option("Restart time (HH:mm:ss)")
        public String Time = "05:35:00";
        @Option("Select config")
        @Editor(JListField.class)
        @Options(ConfigsSupplier.class)
        public String configs = "config";
        @Option(value = "Disable after auto restart", description = "After auto restart the module will auto disable")
        public boolean disableInRestart = false;
        @Option(value = "Auto hide API", description = "After auto restart the API will auto hide")
        public boolean autoHideApi = false;
    }
}
