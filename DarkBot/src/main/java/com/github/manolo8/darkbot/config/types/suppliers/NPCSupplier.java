package com.github.manolo8.darkbot.config.types.suppliers;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.config.annotations.Dropdown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NPCSupplier implements Dropdown.Options<String> {

    @Override
    public Collection<String> options() {
        return Main.getInstance().config.LOOT.NPC_INFOS.keySet();
    }

    @Override
    public @NotNull String getText(@Nullable String option) {
        return option == null ? "" : option;
    }

    @Override
    public String getTooltip(String option) {
        return "";
    }

}
