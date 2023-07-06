package net.hyper.mc.msgbroker.manager;

import lombok.Data;
import net.hyper.mc.msgbroker.Main;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserManager {

    private static UserManager instance;

    public static UserManager getInstance() {
        return instance;
    }

    private List<String> connected = new ArrayList<>();

    public UserManager(){
        instance = this;
    }

    public String connect(){
        String token = Main.createToken(32);
        connected.add(token);
        return token;
    }

    public void disconnect(String token){
        connected.remove(token);
    }
}