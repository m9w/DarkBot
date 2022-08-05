package eu.darkbot.fabio.utils;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigsSupplier extends OptionList<String> {
    private final List<String> configs = new ArrayList<>();

    public ConfigsSupplier() {
        this.configs.add((new File("config.json")).getName().replace(".json", ""));
        File[] files = (new File("configs")).listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) for (File file : files) if (file.isFile()) this.configs.add(file.getName().replace(".json", ""));
    }

    public String getValue(String s) {
        return s;
    }

    public String getText(String s) {
        return s;
    }

    public List<String> getOptions() {
        return this.configs;
    }
}
