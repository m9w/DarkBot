package eu.darkbot.popcorn.def;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.modules.utils.LegacyFlashPatcher;
import java.util.Arrays;

@RegisterFeature("Default plugin")
@Feature(name = "Flash patcher", description = "Installs flash to work again after removal", enabledByDefault = true)
public class FlashPatcher extends LegacyFlashPatcher implements Task {
    public void install(Main main) {
        super.runPatcher();
    }

    public void tick() {}
}
