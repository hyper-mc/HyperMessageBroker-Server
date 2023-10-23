package net.hyper.mc.msgbroker.manager;

import lombok.Data;
import lombok.Getter;
import net.hyper.mc.msgbroker.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserManager {

    @Getter
    private static UserManager instance;

    private Map<String, Long> connected = new ConcurrentHashMap<>();

    public UserManager(){
        instance = this;
    }

    public String connect(){
        String token = Main.createToken(32);
        connected.put(token, System.currentTimeMillis());
        Main.LOGGER.info("New consumer and producer has connected, your token is "+token+".");
        return token;
    }

    public void disconnect(String token){
        connected.remove(token);
        Main.LOGGER.info("The consumer and producer of Token "+token+" has disconnected.");
    }

    public void remove(String token){
        connected.remove(token);
        Main.LOGGER.info("The consumer and producer of the "+token+" token has been removed for not sending updates for a long time.");
    }

    public void update(String token){
        connected.replace(token, System.currentTimeMillis());
    }
}
