import one.nio.http.HttpClient;
import one.nio.http.HttpException;
import one.nio.http.Request;
import one.nio.net.ConnectionString;
import one.nio.pool.PoolException;

import java.io.IOException;


public class BasicClientActivity {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerActivator.main(new String[]{});

        try (HttpClient client = new HttpClient(new ConnectionString("http://localhost:" + "8080"))) {
            //client.get("http://localhost:8080/api/posts");

            Request request = new Request(Request.METHOD_GET, "http://localhost:8080/api/posts", true);
            client.invoke(request);
        } catch (HttpException | PoolException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
