package com.github.manolo8.darkbot.gui.utils;

import java.io.File;
import java.util.Locale;

public class Strings {

    public static String fileName(String path) {
        if (path == null || path.isEmpty()) return "-";
        int split = path.lastIndexOf(File.separatorChar);
        return split > 0 ? path.substring(split + 1) : path;
    }

    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String toTooltip(String str) {
        if (str != null && str.trim().isEmpty()) return null;
        return str;
    }

    public static String simplifyName(String name) {
        if (!name.matches("^[^\\d]+\\d{1,3}$")) return name;
        return name.replaceAll("\\d{1,3}$", " *");
    }

    public static String fuzzyMatcher(String string) {
        return string
                .replace("x", "")
                .replace("X", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "")
                .replaceAll("m[i1]m[e3][s5][i1][s5]", "mimesis");
    }

    public static boolean isEmpty(String s){
        return s != null && s.isEmpty();
    }
}
