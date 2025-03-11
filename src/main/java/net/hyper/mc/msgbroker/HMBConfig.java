package net.hyper.mc.msgbroker;

import lombok.Data;

@Data
public class HMBConfig {

    private int port = 25365;
    private int poolSize = 4;
    private int cleanDelay = 5;
}
