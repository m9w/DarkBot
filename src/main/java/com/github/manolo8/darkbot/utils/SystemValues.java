package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.config.types.suppliers.BrowserApi;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;

import static com.github.manolo8.darkbot.utils.OSUtil.CURRENT_OS;

/**
 * Magic numbers and another values that depend on OS
 * Use Pair and Triple with same generics for join values in sequence Windows, Linux, MAC
 * Call: SystemValues.get(SystemValues.VALUE)
 * */
public interface SystemValues {
    Pair<BrowserApi, BrowserApi> preferAPI = new Pair<>(BrowserApi.DARK_BOAT, BrowserApi.TANOS_API);
    Pair<Integer, Integer> childSpriteOffset = new Pair<>(0xd0,0xe0);
    Pair<Integer, Integer> spriteSizeOffset = new Pair<>(0x18,0x20);
    Pair<Integer, Integer> Box_MAGIC_NUMBER = new Pair<>(8,16);
    Pair<Integer, Integer> MapManager_NameOffset = new Pair<>(440,0x1d0);
    Pair<String, String> LoginUtils_2D_MODE = new Pair<>("2","0");


    static <T> T get(T single){
        return single;
    }
    static <T> T get(Pair<T, T> pair){
        if(CURRENT_OS.equals(OSUtil.OS.WINDOWS)) return pair.a;
        return pair.b;
    }
    static <T> T get(Triple<T,T,T> triple){
        if(CURRENT_OS.equals(OSUtil.OS.WINDOWS)) return triple.a;
        if(CURRENT_OS.equals(OSUtil.OS.LINUX)) return triple.b;
        return triple.c;
    }
}