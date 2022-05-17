package eu.darkbot.popcorn.def;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.utils.http.Http;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.swing.JComponent;

@RegisterFeature
@Feature(name = "DO client updater", description = "Updates the DO client version darkbot pretends to use", enabledByDefault = true)
public class UserAgentUpdater implements Task, Configurable<UserAgentUpdater.Config> {
    private static final String URL = "http://darkorbit-22-client.bpsecure.com/bpflashclient/windows.x64/repository/Updates.xml";
    private static final Pattern VERSION = Pattern.compile("<version>(.*)</version>", Pattern.CASE_INSENSITIVE);
    private UserAgentUpdater.Config config;

    public void setConfig(UserAgentUpdater.Config config) {
        this.config = config;
        Http.setDefaultUserAgent(config.USER_AGENT);
    }

    public void install(Main main) {}

    public void tick() {
        if (System.currentTimeMillis() > this.config.NEXT_UPDATE) {
            String version = null;
            try {
                version = Http.create(URL).consumeInputStream((in) -> (new BufferedReader(new InputStreamReader(in)))
                        .lines().map(VERSION::matcher).filter(Matcher::find).map((matcher) -> matcher.group(1)).findFirst().orElse(null));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (version == null) this.config.NEXT_UPDATE = System.currentTimeMillis() + 3600000L;
            else {
                this.config.USER_AGENT = "BigpointClient/" + version;
                Http.setDefaultUserAgent(this.config.USER_AGENT);
                this.config.NEXT_UPDATE = System.currentTimeMillis() + 21600000L;
            }

            ConfigEntity.changed();
        }
    }

    public static class JTimeDisplay extends JLabelField {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        public JComponent getComponent() {
            return this;
        }

        public void edit(ConfigField field) {
            long time = field.get();
            this.setText(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(this.formatter));
        }
    }

    public static class Config {
        @Option("User agent (auto-updated)")
        @Editor(JLabelField.class)
        public String USER_AGENT = Http.getDefaultUserAgent();

        @Option("Next update")
        @Editor(UserAgentUpdater.JTimeDisplay.class)
        public long NEXT_UPDATE = System.currentTimeMillis();
    }
}
