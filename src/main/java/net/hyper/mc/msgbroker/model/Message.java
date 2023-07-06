package net.hyper.mc.msgbroker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Message {

    private String id;
    private String sender;
    private Object value;
    private List<String> read;
}
