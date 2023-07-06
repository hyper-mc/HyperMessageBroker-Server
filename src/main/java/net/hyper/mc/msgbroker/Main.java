package net.hyper.mc.msgbroker;

import balbucio.responsivescheduler.RSTask;
import balbucio.responsivescheduler.ResponsiveScheduler;
import co.gongzh.procbridge.Server;
import net.hyper.mc.msgbroker.delegate.MessageChannel;
import net.hyper.mc.msgbroker.logger.LoggerFormat;
import net.hyper.mc.msgbroker.manager.QueueManager;
import net.hyper.mc.msgbroker.manager.UserManager;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Main {

    public static Logger LOGGER = Logger.getGlobal();

    public static void main(String[] args) {
        System.out.println("\n" +
                "  _    _                       __  __                                ____            _             \n" +
                " | |  | |                     |  \\/  |                              |  _ \\          | |            \n" +
                " | |__| |_   _ _ __   ___ _ __| \\  / | ___  ___ ___  __ _  __ _  ___| |_) |_ __ ___ | | _____ _ __ \n" +
                " |  __  | | | | '_ \\ / _ \\ '__| |\\/| |/ _ \\/ __/ __|/ _` |/ _` |/ _ \\  _ <| '__/ _ \\| |/ / _ \\ '__|\n" +
                " | |  | | |_| | |_) |  __/ |  | |  | |  __/\\__ \\__ \\ (_| | (_| |  __/ |_) | | | (_) |   <  __/ |   \n" +
                " |_|  |_|\\__, | .__/ \\___|_|  |_|  |_|\\___||___/___/\\__,_|\\__, |\\___|____/|_|  \\___/|_|\\_\\___|_|   \n" +
                "          __/ | |                                          __/ |                                   \n" +
                "         |___/|_|                                         |___/                                    \n");
        System.out.println("HyperMessageBroker is developed and maintained by the HyperNetwork team.");
        new Main();
    }

    private File cnf = new File("config.yml");
    private HMBConfig config;
    private Server server;
    private ResponsiveScheduler scheduler;
    public Main(){
        try {
            LOGGER.setUseParentHandlers(false);
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new LoggerFormat());
            LOGGER.addHandler(handler);
            LOGGER.info("Loading configuration...");
            if (!cnf.exists()) {
                Files.copy(this.getClass().getResourceAsStream("/config.yml"), cnf.toPath());
            }
            Yaml yaml = new Yaml(new Constructor(HMBConfig.class, new LoaderOptions()));
            config = yaml.load(Files.newInputStream(cnf.toPath()));
            LOGGER.info("Loading user manager...");
            new UserManager();
            LOGGER.info("Loading queue manager...");
            new QueueManager();
            LOGGER.info("Starting server in port "+config.getPort());
            this.server = new Server(config.getPort(), new MessageChannel());
            server.start();
            LOGGER.info("Done! You can now connect your servers in the message broker.");
            scheduler = new ResponsiveScheduler();
            scheduler.repeatTask(new RSTask(){
                @Override
                public void run(){
                    try {
                        QueueManager.getInstance().getMessages().forEach((q, l) -> {
                            l.forEach(m -> {
                                if (m.getRead().size() >= (UserManager.getInstance().getConnected().size() - 1)) {
                                    QueueManager.getInstance().getMessages().get(q).remove(m);
                                    Main.LOGGER.info("Everyone marked the message ID " + m.getId() + " as read, so it was deleted. (" + QueueManager.getInstance().getMessages().get(q).size() + ")");
                                }
                            });
                        });
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, 0, 10000);
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