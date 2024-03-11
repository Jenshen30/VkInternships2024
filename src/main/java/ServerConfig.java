import java.nio.file.Path;

public final class ServerConfig {
    private final int selfPort;
    private final String selfUrl;
    private final Path workingDir;

    public ServerConfig(
            int selfPort,
            String selfUrl,
            Path workingDir
    ) {
        this.selfPort = selfPort;
        this.selfUrl = selfUrl;
        this.workingDir = workingDir;
    }

    public int selfPort() {
        return selfPort;
    }

    public String selfUrl() {
        return selfUrl;
    }


    public Path workingDir() {
        return workingDir;
    }
}