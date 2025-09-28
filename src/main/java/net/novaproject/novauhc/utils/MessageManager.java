package net.novaproject.novauhc.utils;

import java.util.HashMap;
import java.util.UUID;

public class MessageManager {
    private static final HashMap<UUID, UUID> lastMessage = new HashMap<>();

    public static void setLastMessage(UUID sender, UUID receiver) {
        lastMessage.put(sender, receiver);
        lastMessage.put(receiver, sender);
    }

    public static UUID getLastMessage(UUID player) {
        return lastMessage.get(player);
    }
}
