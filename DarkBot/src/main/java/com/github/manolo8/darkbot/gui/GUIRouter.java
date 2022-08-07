package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.features.handlers.DrawableHandler;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.plugins.PluginCard;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.login.LoginData;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Module;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;

public interface GUIRouter {
    GUIRouter router = init();
    static GUIRouter getInstance() {
        return router;
    }

    static GUIRouter init() {
        if(Main.params.has(StartupParams.LaunchArg.NO_GUI)) return new NoGUI();
        return new GUIRouter(){};
    }

    default PopupsUtils getPopups() {
        return new PopupsUtils(){};
    }
    default Optional<LoginData> getLoginData(){
        return Optional.empty();
    }
    default MainGui getGUI(Main main){
        return new com.github.manolo8.darkbot.gui.MainGui(main);
    }
    default PidSelector getPidSelector(GameAPI.Window.Proc[] procs) {
        return new com.github.manolo8.darkbot.gui.utils.PidSelector(procs);
    }
    default Class<?>[] getDrawables(){
        return DrawableHandler.NATIVE;
    }
    default PluginDisplay getPluginDisplay() {
        return new PluginDisplay();
    }
    default PluginCard getPluginCard(Main main, Plugin pl, FeatureRegistry featureRegistry){
        return new PluginCard(main,pl, featureRegistry);
    }

    abstract class MainGui extends JFrame {
        public MainGui() {}
        public MainGui(String title) throws HeadlessException {
            super(title);
        }
        public abstract void addConfigVisibilityListener(Consumer<Boolean> listener);
        public abstract void toggleConfig();
        public abstract void setCustomConfig(@Nullable ConfigSetting.Parent<?> config);
        public abstract void updateConfigTreeListeners();
        public abstract void updateConfiguration();
        public abstract void tryClose();
        public abstract void tick();
        public abstract void setCustomTitleButtons(Module module);
        public void setTitle(String title) {
            super.setTitle(title);
        }
    }

    interface PopupsUtils {
        default boolean isEventDispatchThread() {
            return SwingUtilities.isEventDispatchThread();
        }

        default void invokeAndWait(Runnable doRun) throws InterruptedException, InvocationTargetException {
            SwingUtilities.invokeAndWait(doRun);
        }

        default void invokeLater(Runnable doRun) {
            SwingUtilities.invokeLater(doRun);
        }
    }

    interface PidSelector {
        Integer getPid() throws NumberFormatException;
    }
}
