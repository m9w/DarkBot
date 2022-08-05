package eu.darkbot.fabio.api;

public class SchifoAPI {
    public SchifoAPI() {
    }

    public static native void sendCommand(String var0);

    public static native String version();

    public static native void showHangar(String var0, String var1, String var2);

    public static native void BackPage(String var0, String var1);

    static {
        System.load(System.getProperty("java.io.tmpdir") + "SchifoAPI" + ManageAPI.random + ".dll");
        ManageAPI.loaded = true;
    }
}
