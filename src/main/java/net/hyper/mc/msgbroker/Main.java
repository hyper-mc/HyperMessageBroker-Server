package net.hyper.mc.msgbroker;

import co.gongzh.procbridge.Server;
import net.hyper.mc.msgbroker.delegate.MessageChannel;
import net.hyper.mc.msgbroker.manager.QueueManager;
import net.hyper.mc.msgbroker.manager.UserManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;

public class Main {

    public static Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        System.out.println("\n" +
                "█████████████████████████████████████████████████████████████████▀██████████████████████████████████████████\n" +
                "█─█─█▄─█─▄█▄─▄▄─█▄─▄▄─█▄─▄▄▀█▄─▀█▀─▄█▄─▄▄─█─▄▄▄▄█─▄▄▄▄██▀▄─██─▄▄▄▄█▄─▄▄─█▄─▄─▀█▄─▄▄▀█─▄▄─█▄─█─▄█▄─▄▄─█▄─▄▄▀█\n" +
                "█─▄─██▄─▄███─▄▄▄██─▄█▀██─▄─▄██─█▄█─███─▄█▀█▄▄▄▄─█▄▄▄▄─██─▀─██─██▄─██─▄█▀██─▄─▀██─▄─▄█─██─██─▄▀███─▄█▀██─▄─▄█\n" +
                "▀▄▀▄▀▀▄▄▄▀▀▄▄▄▀▀▀▄▄▄▄▄▀▄▄▀▄▄▀▄▄▄▀▄▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▀▄▄▀");
        System.out.println("HyperMessageBroker is developed and maintained by the HyperNetwork team.");
        new Main();
    }

    private File cnf = new File("config.yml");
    private HMBConfig config;
    private Server server;
    public Main(){
        try {
            LOGGER.info("Loading configuration...");
            if (!cnf.exists()) {
                Files.copy(this.getClass().getResourceAsStream("/config.yml"), cnf.toPath());
            }
            Yaml yaml = new Yaml(new Constructor(HMBConfig.class, new LoaderOptions()));
            config = yaml.load(Files.newInputStream(cnf.toPath()));
            new UserManager();
            new QueueManager();
            this.server = new Server(config.getPort(), new MessageChannel());
            server.start();
            LOGGER.info("Done! You can now connect your servers in the message broker.");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String createToken(int size){
        Random random = new Random();
        int leftLimit = 48;
        int rightLimit = 122;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(size)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }
}