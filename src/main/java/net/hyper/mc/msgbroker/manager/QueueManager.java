package net.hyper.mc.msgbroker.manager;

import lombok.Data;
import net.hyper.mc.msgbroker.Main;
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

    private static QueueManager instance;

    public static QueueManager getInstance() {
        return instance;
    }

    private ConcurrentHashMap<String, List<Message>> messages = new ConcurrentHashMap<>();

    public QueueManager(){
        instance = this;
    }

    public JSONObject createMessage(String queue, String creator, Object value){
        if (!messages.containsKey(queue)) {
            messages.put(queue, new CopyOnWriteArrayList<>());
        }
        Message msg = new Message(Main.createToken(24), creator, value, new ArrayList<>());
        messages.get(queue).add(msg);
        Main.LOGGER.info("Message of ID "+msg.getId()+" was created by sender of ID "+msg.getSender()+".");
        return new JSONObject().put("id", msg.getId());
    }

    public JSONObject getUpdates(String queue, String consumer){
        List<Message> nonRead = messages.getOrDefault(queue, new ArrayList<>()).stream()
                .filter(m -> !m.getRead().stream().anyMatch(c -> c.equalsIgnoreCase(consumer)) && !m.getSender().equalsIgnoreCase(consumer))
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

    public void confirmRead(String queue, String consumer, String id){
        messages.get(queue).stream().filter(m -> m.getId().equalsIgnoreCase(id)).forEach(m -> {
            m.getRead().add(consumer);
        });
    }
}
