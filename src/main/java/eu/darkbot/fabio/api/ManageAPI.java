package eu.darkbot.fabio.api;

import eu.darkbot.fabio.modules.BotUpdater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class ManageAPI {
    public static int random;
    public static boolean deleted;
    public static boolean loaded;
    public static boolean checked;

    public ManageAPI() {
        File f = new File("lib\\SchifoAPI.dll");
        if (!f.exists()) downloadAPI();
        random = (int)(Math.random() * 10000.0D) + 1;
        Path source = Paths.get("lib\\SchifoAPI.dll");

        try {
            Files.copy(source, source.resolveSibling(System.getProperty("java.io.tmpdir") + "SchifoAPI" + random + ".dll"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkApiVersion() {
        try {
            if (!SchifoAPI.version().equals(BotUpdater.getVersion("API_version"))) downloadAPI();
            checked = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void downloadAPI() {
        loader("https://gist.github.com/fabio1999ita/d3c47965a1f2758a44dc6f6fdd2fccf9/raw/SchifoAPI.dll", "lib\\SchifoAPI.dll", false);
    }

    public static void deleteTmpFile() {
        new File("lib\\SchifoAPI.dll");
        File downloadDirectory = new File(System.getProperty("java.io.tmpdir"));
        File[] files = downloadDirectory.listFiles((dir, name) -> name.matches("SchifoAPI.*?"));
        assert files != null;
        Arrays.asList(files).forEach(File::delete);
        deleted = true;
    }

    public static void init(){
        if (!deleted) ManageAPI.deleteTmpFile();
        if (!loaded) new ManageAPI();
        if (!checked) ManageAPI.checkApiVersion();
    }

    public static void loader(String from, String to, boolean checkExisting){
        File flash = new File(to);
        if (!checkExisting || !flash.exists()) {
            try (BufferedInputStream in = new BufferedInputStream((new URL(from)).openStream())) {
                try (FileOutputStream out = new FileOutputStream(to)) {
                    byte[] data = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1) out.write(data, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
