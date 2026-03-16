package net.hyper.mc.msgbroker.manager;

import lombok.Data;
import lombok.Getter;
import net.hyper.mc.msgbroker.Main;
import net.hyper.mc.msgbroker.HMBConfig;
import net.hyper.mc.msgbroker.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
public class QueueManager {

    @Getter
    private static QueueManager instance;

    private final ConcurrentHashMap<String, List<Message>> messages = new ConcurrentHashMap<>();
    private final long messageTtlMs;
    private final int maxMessagesPerQueue;

    public QueueManager(HMBConfig config) {
        instance = this;
        this.messageTtlMs = config.getMessageTtlSeconds() * 1000L;
        this.maxMessagesPerQueue = config.getMaxMessagesPerQueue();
    }

    public JSONObject createMessage(String queue, String creator, Object value) {
        if (!UserManager.getInstance().isValid(creator)) {
            Main.LOGGER.warning("Rejected message creation for invalid token " + creator);
            return new JSONObject().put("error", "invalid_token");
        }
        if (!messages.containsKey(queue)) {
            messages.put(queue, new CopyOnWriteArrayList<>());
        }
        List<Message> queueMessages = messages.get(queue);
        trimQueueIfNeeded(queueMessages, queue);
        Message msg = new Message(Main.createToken(24), creator, value, new CopyOnWriteArrayList<>(), System.currentTimeMillis());
        queueMessages.add(msg);
        Main.LOGGER.info("Message of ID " + msg.getId() + " was created by sender of ID " + msg.getSender() + ".");
        return new JSONObject().put("id", msg.getId());
    }

    public JSONObject getUpdates(String queue, String consumer) {
        if (!UserManager.getInstance().isValid(consumer)) {
            Main.LOGGER.warning("Rejected update request for invalid token " + consumer);
            return new JSONObject().put("error", "invalid_token");
        }
        List<Message> nonRead = messages.getOrDefault(queue, new ArrayList<>()).stream()
                .filter(m -> m.getRead().stream().noneMatch(c -> c.equalsIgnoreCase(consumer)) && !m.getSender().equalsIgnoreCase(consumer))
                .collect(Collectors.toList());
        JSONObject packet = new JSONObject();
        packet.put("token", consumer);
        JSONArray array = new JSONArray();
        nonRead.forEach(m -> array.put(new JSONObject()
                .put("id", m.getId())
                .put("creator", m.getSender())
                .put("value", m.getValue())));
        packet.put("msgs", array);
        //Main.LOGGER.info("The consumer of Token "+consumer+" has "+nonRead.size()+" messages non readed.");
        return packet;
    }

    public void confirmRead(String queue, String consumer, String id) {
        if (!UserManager.getInstance().isValid(consumer)) {
            Main.LOGGER.warning("Rejected read confirmation for invalid token " + consumer);
            return;
        }
        List<Message> queueMessages = messages.get(queue);
        if (queueMessages == null) {
            return;
        }
        queueMessages.stream()
                .filter(m -> m.getId().equalsIgnoreCase(id))
                .findFirst()
                .ifPresent(m -> m.getRead().add(consumer));
    }

    public void removeOldMessages() {
        long now = System.currentTimeMillis();
        int connectedCount = UserManager.getInstance().connectedCount();
        messages.forEach((queueName, msgs) -> {
            if (msgs.isEmpty()) {
                return;
            }
            boolean changed = msgs.removeIf(message -> shouldRemove(message, connectedCount, now));
            if (changed) {
                Main.LOGGER.fine("Cleaned queue " + queueName + " remaining size: " + msgs.size());
            }
        });
    }

    private void trimQueueIfNeeded(List<Message> queueMessages, String queueName) {
        while (queueMessages.size() >= maxMessagesPerQueue) {
            Message removed = queueMessages.remove(0);
            Main.LOGGER.warning("Queue " + queueName + " exceeded " + maxMessagesPerQueue + " messages. Dropping oldest message " + removed.getId());
        }
    }

    private boolean shouldRemove(Message message, int connectedCount, long now) {
        boolean expiredByTime = (now - message.getCreatedAt()) > messageTtlMs;
        int requiredReads = Math.max(connectedCount - 1, 0);
        boolean readByEveryone = requiredReads > 0 && message.getRead().size() >= requiredReads;
        if (expiredByTime || readByEveryone) {
            Main.LOGGER.info("Removing message " + message.getId() + " expired=" + expiredByTime + ", readByEveryone=" + readByEveryone);
            return true;
        }
        return false;
    }
}
