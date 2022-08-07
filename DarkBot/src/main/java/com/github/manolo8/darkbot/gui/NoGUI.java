package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.plugins.PluginCard;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.utils.login.LoginData;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Module;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;
import java.util.function.Consumer;

public class NoGUI implements GUIRouter {
    @Override
    public PopupsUtils getPopups() {
        return new PopupsUtils() {
            @Override
            public boolean isEventDispatchThread() {
                return false;
            }

            @Override
            public void invokeAndWait(Runnable doRun) {}

            @Override
            public void invokeLater(Runnable doRun) {}
        };
    }

    @Override
    public Optional<LoginData> getLoginData() {
        throw new NotImplementedException("GUI Login is no available");
    }

    @Override
    public MainGui getGUI(Main main) {
        return new MainGui() {
            @Override
            public void addConfigVisibilityListener(Consumer<Boolean> listener) { }

            @Override
            public void toggleConfig() { }

            @Override
            public void setCustomConfig(ConfigSetting.@Nullable Parent<?> config) { }

            @Override
            public void updateConfigTreeListeners() { }

            @Override
            public void updateConfiguration() { }

            @Override
            public void tryClose() { }

            @Override
            public void setTitle(String title) { }

            @Override
            public void tick() { }

            @Override
            public void setCustomTitleButtons(Module module) { }
        };
    }

    @Override
    public PidSelector getPidSelector(GameAPI.Window.Proc[] procs) {
        return () -> 0;
    }

    @Override
    public Class<?>[] getDrawables() {
        return new Class[0];
    }

    @Override
    public PluginDisplay getPluginDisplay() {
        return new PluginDisplay() {
            @Override
            public void refreshUI() { }

            @Override
            public PluginCard getPluginCard(Plugin plugin) {
                return new PluginCard() {
                    @Override
                    public Plugin getPlugin() {
                        return plugin;
                    }

                    @Override
                    public void setProgressBarMaximum(int max) { }
                };
            }

            @Override
            public JProgressBar getMainProgressBar() {
                return new JProgressBar();
            }

            @Override
            public void setup(Main main, MainButton pluginTab) { }
        };
    }
}
