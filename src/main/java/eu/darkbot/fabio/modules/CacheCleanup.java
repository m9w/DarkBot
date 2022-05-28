package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import eu.darkbot.fabio.api.SchifoAPI;
import eu.darkbot.fabio.api.ManageAPI;

import java.io.IOException;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "CacheCleanup", description = "Auto cache cleanup for Darkbot")
public class CacheCleanup implements Task {
    @Override
    public void install(Main main) {
        if (Main.VERSION.getBeta() >= 100) {
            ManageAPI.init();

            SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
            SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
            SchifoAPI.sendCommand("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");
        } else {
            try {
                Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
                Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 8");
                Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 16");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void tick() {}
}