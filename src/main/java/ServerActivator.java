import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class ServerActivator {
    private ServerActivator() {

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ProxyServer server = new ProxyServer(new ServerConfig(
                8080, "http://localhost/",
                Files.createTempDirectory(".")
        ));
        server.start();
        Thread.sleep(100);
        //server.stop();

    }
}