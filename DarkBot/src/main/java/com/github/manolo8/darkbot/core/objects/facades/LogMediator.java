package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.Lazy;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.GameLogAPI;

import java.time.LocalDateTime;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.utils.LogUtils.LOG_DATE;

public class LogMediator extends Updatable implements GameLogAPI, Listener {
    @Deprecated
    public final Lazy<String> logs = new Lazy.NoCache<>(); // Can't cache the value, same log could appear twice
    private final ObjArray messageBuffer = ObjArray.ofArrStr();
    private final EventBrokerAPI eventBroker;
    public long serverReboot = -1;

    public LogMediator(EventBrokerAPI eventBroker) {
        this.eventBroker = eventBroker;
        this.eventBroker.registerListener(this);
    }

    @Override
    public void update() {
        messageBuffer.update(API.readMemoryLong(address + 0x60));
        if (messageBuffer.size <= 0 || 50 < messageBuffer.size) return;

        messageBuffer.forEachIncremental(this::handleLogMessage);
    }

    private void handleLogMessage(long pointer) {
        String val = API.readMemoryString(API.readMemoryLong(pointer + 0x28));
        if (val != null && !val.trim().isEmpty())
            eventBroker.sendEvent(new LogMessageEvent(val));
    }

    @EventHandler
    public void onLogMessage(LogMessageEvent e) {
        if(e.getMessage().equals("Server restarting...")) serverReboot = System.currentTimeMillis();
        if(e.getMessage().endsWith("INTEX!")) serverReboot = -1;
        System.out.println("[" + LocalDateTime.now().format(LOG_DATE) + "] " + e.getMessage());
        logs.send(e.getMessage());
    }

}
