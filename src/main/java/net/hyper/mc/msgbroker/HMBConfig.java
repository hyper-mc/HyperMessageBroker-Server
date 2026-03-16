package net.hyper.mc.msgbroker;

import lombok.Data;

@Data
public class HMBConfig {

    private int port = 25365;
    private int poolSize = 4;
    private int cleanDelay = 5;
    /** Max time (in seconds) a client can stay without sending ONLINE before being kicked. */
    private int tokenTimeoutSeconds = 10;
    /** Max time (in seconds) a message stays stored before being dropped. */
    private int messageTtlSeconds = 120;
    /** Upper bound of queued messages kept per queue to avoid unbounded memory usage. */
    private int maxMessagesPerQueue = 1000;
}
