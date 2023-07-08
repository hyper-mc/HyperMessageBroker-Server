package net.hyper.mc.msgbroker.delegate;

import co.gongzh.procbridge.IDelegate;
import net.hyper.mc.msgbroker.manager.QueueManager;
import net.hyper.mc.msgbroker.manager.UserManager;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class MessageChannel implements IDelegate {
    @Override
    public @Nullable Object handleRequest(@Nullable String s, @Nullable Object o) {
        JSONObject payload = new JSONObject((String) o);
        if(s.equalsIgnoreCase("UPDATE")){
            return QueueManager.getInstance().getUpdates(payload.getString("queue"), payload.getString("token"));
        } else if(s.equalsIgnoreCase("CREATE")){
            return QueueManager.getInstance().createMessage(payload.getString("queue"), payload.getString("token"),  payload.get("value"));
        } else if(s.equalsIgnoreCase("CONNECT")){
            return UserManager.getInstance().connect();
        } else if(s.equalsIgnoreCase("DISCONNECT")){
            UserManager.getInstance().disconnect(payload.getString("token"));
        } else if(s.equalsIgnoreCase("READ")){
            QueueManager.getInstance().confirmRead(payload.getString("queue"), payload.getString("token"), payload.getString("id"));
        }
        return new JSONObject().toString();
    }
}
