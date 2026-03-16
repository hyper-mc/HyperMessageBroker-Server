package net.hyper.mc.msgbroker.manager;

import lombok.Data;
import lombok.Getter;
import net.hyper.mc.msgbroker.Main;
import net.hyper.mc.msgbroker.HMBConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserManager {

    @Getter
    private static UserManager instance;

    private final Map<String, Long> connected = new ConcurrentHashMap<>();
    private final long tokenTimeoutMs;

    public UserManager(HMBConfig config) {
        instance = this;
        this.tokenTimeoutMs = config.getTokenTimeoutSeconds() * 1000L;
    }

    public String connect() {
        String token = Main.createToken(32);
        connected.put(token, System.currentTimeMillis());
        Main.LOGGER.info("New consumer and producer has connected, your token is " + token + ".");
        return token;
    }

    public void disconnect(String token) {
        connected.remove(token);
        Main.LOGGER.info("The consumer and producer of Token " + token + " has disconnected.");
    }

    public void remove(String token) {
        connected.remove(token);
        Main.LOGGER.info("The consumer and producer of the " + token + " token has been removed for not sending updates for a long time.");
    }

    public void update(String token) {
        if (connected.replace(token, System.currentTimeMillis()) == null) {
            Main.LOGGER.warning("Attempt to update unknown token " + token + ". Ignoring.");
        }
    }

    public boolean isValid(String token) {
        return connected.containsKey(token);
    }

    public void removeAfk() {
        if (connected.isEmpty()) {
            return;
        }
        connected.forEach((token, time) -> {
            long limit = time + tokenTimeoutMs;
            if (System.currentTimeMillis() > limit) {
                remove(token);
            }
        });
    }

    public int connectedCount() {
        return connected.size();
    }
}
