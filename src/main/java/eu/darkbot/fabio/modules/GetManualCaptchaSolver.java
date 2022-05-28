package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import eu.darkbot.fabio.api.ManageAPI;

import static eu.darkbot.fabio.api.ManageAPI.loader;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "getManualCaptchaSolver [BETA]", description = "Just download manual captcha solver lib for you")
public class GetManualCaptchaSolver implements Task {
    public GetManualCaptchaSolver() {
    }

    public void install(Main main) {
        ManageAPI.init();
        loader("https://host.darkbot.eu/uploads/Fabio/captchasolver.jar", "lib\\captchasolver.jar", true);
    }

    public void tick() {}
}
