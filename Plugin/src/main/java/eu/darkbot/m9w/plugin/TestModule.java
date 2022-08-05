package eu.darkbot.m9w.plugin;

import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.RegisterFeature;
import eu.darkbot.api.extensions.Module;

@RegisterFeature
@Feature(name = "test module", description = "desc")
public class TestModule implements Module {
    @Override
    public void onTickModule() {

    }

    @Override
    public String getStatus() {
        return "Simple module, that do nothing. Just for for testing";
    }
}
