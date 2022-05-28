package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.fabio.api.ManageAPI;
import eu.darkbot.fabio.api.SchifoAPI;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;

import static eu.darkbot.fabio.api.ManageAPI.loader;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "BackPage [BETA]", description = "BackPage for homepage of darkorbit with flash integrated")
public class BackPage implements Task, ExtraMenuProvider {
    public BackPage() {
    }

    public void install(Main main) {
        ManageAPI.init();
    }

    public void tick() {}

    public Collection<JComponent> getExtraMenuItems(Main main) {
        return Arrays.asList(this.createSeparator("Schifo Browser"), this.create("BackPage", (e) -> this.logic(main, main.statsManager.instance, main.statsManager.sid)));
    }

    private void logic(Main main, String instance, String sid) {
        loader("https://host.darkbot.eu/uploads/Fabio/BackPage.jar", "lib\\BackPage.jar", true);
        loader("https://host.darkbot.eu/uploads/Fabio/pepflashplayer.dll", "lib\\pepflashplayer.dll", true);

        if (main.backpage.sidStatus().contains("OK")) {
            SchifoAPI.BackPage(instance, sid);
        } else {
            Popups.showMessageAsync("Error", "Your SID must be OK to see the hangar.\nTry a manual reload or restart the bot.", 0);
        }
    }
}
