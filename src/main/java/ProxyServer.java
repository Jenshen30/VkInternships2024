import one.nio.http.*;

import one.nio.server.AcceptorConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

public class ProxyServer extends HttpServer {

    private static final int CORE_POOL = 4;
    private static final int MAX_POOL = 8;

    private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(256);
    private final ExecutorService executorService =
            new ThreadPoolExecutor(CORE_POOL, MAX_POOL,
                    10L, TimeUnit.MILLISECONDS,
                    queue);
    private static final Response ACCEPTED = new Response(Response.ACCEPTED, Response.EMPTY);
    private final ServerConfig serviceConfig;
    private static final Response BAD = new Response(Response.BAD_REQUEST, Response.EMPTY);

    public ProxyServer(ServerConfig conf) throws IOException {
        super(convertToHttpConfig(conf));
        this.serviceConfig = conf;
    }

    private static HttpServerConfig convertToHttpConfig(ServerConfig conf) {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.port = conf.selfPort();
        acceptorConfig.reusePort = true;

        HttpServerConfig httpServerConfig = new HttpServerConfig();
        httpServerConfig.closeSessions = true;
        httpServerConfig.acceptors = new AcceptorConfig[]{acceptorConfig};
        return httpServerConfig;
    }


    private static void closeExecutorService(ExecutorService exec) {
        if (exec == null) {
            return;
        }

        exec.shutdownNow();
        while (!exec.isTerminated()) {
            try {
                if (exec.awaitTermination(20, TimeUnit.SECONDS)) {
                    exec.shutdownNow();
                }

            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    @Path(value = "/api/posts/")
    @RequestMethod(Request.METHOD_GET) // fixme delete!!!
    public Response getEntry() {
        System.out.println("YEEEES!!!");
        return new Response("200", new byte[]{});
    }

    @Override
    public void handleRequest(Request request, HttpSession session) throws IOException {
        try {

            executorService.execute(() -> {
                try {
                    super.handleRequest(request, session);
                } catch (RuntimeException e) {
                    errorAccept(session, e, Response.BAD_REQUEST);
                } catch (IOException e) {
                    errorAccept(session, e, Response.CONFLICT);
                }
            });
        } catch (RejectedExecutionException e) {
            session.sendError(Response.CONFLICT, e.toString());
        }
    }

    private void errorAccept(HttpSession session, Exception e, String message) {
        try {
            session.sendError(message, e.toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void handleDefault(Request request, HttpSession session) throws IOException {
        int method = request.getMethod();
        Response response;

        if (method == Request.METHOD_PUT
                || method == Request.METHOD_DELETE
                || method == Request.METHOD_GET) {

            response = BAD;
        } else {
            response = new Response(Response.METHOD_NOT_ALLOWED, Response.EMPTY);
        }
        session.sendResponse(response);
    }
}