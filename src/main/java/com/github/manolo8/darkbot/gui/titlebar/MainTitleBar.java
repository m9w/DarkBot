package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainTitleBar extends JPanel implements SimpleMouseListener {
    final Set<TitleBarButton<JFrame>> customButtons = new HashSet<>();

    public MainTitleBar(Main main, MainGui frame) {
        super(new MigLayout("ins 0, gap 0, fill", "[][][][][grow, 30px::][][][][][][][]", "[]"));
        add(new ExtraButton(main, frame), "grow");
        add(new ConfigButton(frame), "grow");
        add(new StatsButton(frame), "grow, hidemode 2");
        add(new StartButton(main, frame), "grow");

        add(new DragArea(frame), "grow");

        add(new HookButton(frame), "grow, hidemode 2");
        add(new DiagnosticsButton(main, frame), "grow");
        add(new VisibilityButton(main, frame), "grow");
        add(new PinButton(frame), "grow");
        add(new TrayButton(main, frame), "grow, hidemode 2");
        add(new MinimizeButton(frame), "grow");
        add(new MaximizeButton(frame), "grow");
        add(new CloseButton(frame), "grow");
    }

    public void addCustomButton(MainGui frame, AddButtonsToTitleBar.enumButtons button) {
        TitleBarButton<JFrame> customButton = new TitleBarButton<JFrame>(UIUtils.getIcon(button.getIcon()), frame){
            public void actionPerformed(ActionEvent e) { super.actionPerformed(e); button.getAction().run(); }
        };
        customButton.setToolTipText(button.toString().replace('_',' '));
        customButtons.add(customButton);
        add(customButton, "grow", customButtons.size()+4);
    }

    public void removeCustomButtons(){
        customButtons.stream().filter(Objects::nonNull).forEach(this::remove);
        customButtons.clear();
    }
}
