package net.hyper.mc.msgbroker.delegate;

import co.gongzh.procbridge.server.IDelegate;
import net.hyper.mc.msgbroker.manager.QueueManager;
import net.hyper.mc.msgbroker.manager.UserManager;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class MessageChannel implements IDelegate {
    @Override
    public @Nullable Object handleRequest(@Nullable String s, @Nullable Object o) {
        if (s == null || o == null) {
            return new JSONObject().put("error", "empty_request").toString();
        }

        JSONObject payload = new JSONObject((String) o);
        switch (s.toUpperCase()) {
            case "UPDATE":
                return QueueManager.getInstance().getUpdates(payload.getString("queue"), payload.getString("token"));
            case "CREATE":
                return QueueManager.getInstance().createMessage(payload.getString("queue"), payload.getString("token"), payload.get("value"));
            case "CONNECT":
                return UserManager.getInstance().connect();
            case "DISCONNECT":
                UserManager.getInstance().disconnect(payload.getString("token"));
                return new JSONObject().put("status", "disconnected");
            case "READ":
                QueueManager.getInstance().confirmRead(payload.getString("queue"), payload.getString("token"), payload.getString("id"));
                return new JSONObject().put("status", "read");
            case "ONLINE":
                UserManager.getInstance().update(payload.getString("token"));
                return new JSONObject().put("status", "ok");
            default:
                return new JSONObject().put("error", "unknown_action").put("action", s).toString();
        }
    }
}
