package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import eu.darkbot.api.extensions.RegisterFeature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.http.Method;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.ManageAPI;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "HangarView", description = "Open graphic hangar in backpage")
public class HangarView implements Task, ExtraMenuProvider {
    private Main main;
    public void install(Main main) {
        ManageAPI.init();
    }

    public Collection<JComponent> getExtraMenuItems(Main main) {
        this.main = main;
        return Arrays.asList(this.createSeparator("Schifo"), this.create("Show Hangar", this::logic));
    }

    public void tick() {}

    private void logic(ActionEvent actionEvent) {
        if (main.backpage.sidStatus().contains("OK")) try {
            String flashEmbed = (main.backpage.getConnection("indexInternal.es?action=internalDock", Method.GET)
                    .consumeInputStream((inputStream) -> (new BufferedReader(new InputStreamReader(inputStream)))
                            .lines().filter((l) -> l.contains("flashembed(\"equipment_container\""))
                            .findFirst()
                            .orElse(null)))
                    .split("}, \\{")[1]
                    .replaceAll(",", "&")
                    .replaceAll(": ", "=")
                    .replaceAll("\"", "")
                    .replaceAll("}\\);", "");
            SchifoAPI.showHangar(main.statsManager.instance, main.statsManager.sid, flashEmbed);
        } catch (IOException e) {
            e.printStackTrace();
        } else Popups.showMessageAsync("Error", "Your SID must be OK to see the hangar.\nTry a manual reload or restart the bot.", 0);
    }
}
