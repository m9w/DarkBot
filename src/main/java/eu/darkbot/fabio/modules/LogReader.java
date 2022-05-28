package eu.darkbot.fabio.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.RegisterFeature;
import com.github.manolo8.darkbot.gui.utils.Popups;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterFeature(value = "Schifo", author = "Fabio")
@Feature(name = "LogReader", description = "displays the log files in order of modification")
public class LogReader implements Task, InstructionProvider, ExtraMenuProvider {
    private JPanel panel;
    private JTextArea area;

    public void install(Main main) {
        this.panel = new JPanel(new MigLayout(""));
        this.area = new JTextArea();
        JScrollPane scroll = new JScrollPane(this.area);
        this.panel.add(scroll, "height ::" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.2D + ", width ::" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 1.5D);
    }

    public void tick() {}

    public Collection<JComponent> getExtraMenuItems(Main main) {
        return !main.featureRegistry.getFeatureDefinition("eu.darkbot.fabio.modules.HangarView")
                .isEnabled()
                ?
                Arrays.asList(this.createSeparator("Schifo"), this.create("Log Reader", (e) -> this.logic()))
                :
                Collections.singletonList(this.create("Log Reader", (e) -> this.logic()));
    }

    private void logic() {
        if (!this.area.getText().trim().equals("")) {
            this.area.selectAll();
            this.area.replaceSelection("");
        }
        List<File> files = this.listFilesOldestFirst();
        for(File file : files) {
            if(!file.isFile()) {
                if (files.get(files.size() - 1).equals(file)) {
                    DefaultCaret caret = (DefaultCaret)this.area.getCaret();
                    caret.setUpdatePolicy(1);
                    Popups.showMessageAsync("Darkbot Log", new Object[]{this.panel}, -1);
                    return;
                } else continue;
            }
            try {
                Files.readAllLines(file.toPath()).forEach(s -> area.append(s + "\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<File> listFilesOldestFirst() {
        try (Stream<Path> fileStream = Files.list(Paths.get("logs"))) {
            return fileStream.map(Path::toFile).sorted(Comparator.comparing(File::lastModified).reversed()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
