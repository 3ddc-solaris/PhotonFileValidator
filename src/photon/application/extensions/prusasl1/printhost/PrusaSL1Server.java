package photon.application.extensions.prusasl1.printhost;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import photon.application.extensions.prusasl1.printhost.utilites.FormDataHandler;
import photon.application.extensions.prusasl1.printhost.utilites.PrintHostTemporaryFile;
import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class PrusaSL1Server {
    public interface EventListener {
        void onNewFile(final File file);
    }

    private static volatile PrusaSL1Server instance = null;

    private volatile HttpServer server = null;
    private volatile boolean enabled = true;
    private EventListener eventListener = null;
    private String message = "Print Host is not running";

    private PrusaSL1Server() {}

    public static synchronized PrusaSL1Server getInstance() {
        if(null == instance) {
            instance = new PrusaSL1Server();
        }
        return instance;
    }

    public synchronized void start() {
        if(!PrusaSL1Configuration.getInstance().isPrusaSL1ServerEnabled() || null != server)
            return;

        setMessage("Starting Print Host on port " + PrusaSL1Configuration.getInstance().getPrusaSL1ServerPort());

        try {
            server = HttpServer.create(new InetSocketAddress(PrusaSL1Configuration.getInstance().getPrusaSL1ServerPort()), 0);
            server.createContext("/api/version", httpExchange -> {
                reply(httpExchange, 200,
                        "{\n" +
                                "  \"api\": \"0.1\",\n" +
                                "  \"server\": \"1.3.10\",\n" +
                                "  \"text\": \"Prusa SLA 1.3.10\"\n" +
                                "}");
            });

            server.createContext("/api/files/local", new FormDataHandler() {

                @Override
                public void handle(HttpExchange httpExchange, List<MultiPart> parts) throws IOException {
                    if(!isEnabled()) {
                        reply(httpExchange, 400, "Server is temporary disabled");
                        return;
                    }

                    for(final MultiPart part : parts) {
                        if(!PartType.FILE.equals(part.type))
                            continue;

                        if(null == part.contentType || !part.contentType.equals("application/octet-stream")) {
                            reply(httpExchange, 415, "Unsupported Content Type: " + part.contentType);
                            return;
                        }

                        if(null == part.filename || !part.filename.toLowerCase().endsWith(".sl1")) {
                            reply(httpExchange, 415, "Unsupported File Type: " + part.filename);
                            return;
                        }

                        if(null == part.bytes || 0 == part.bytes.length) {
                            reply(httpExchange, 400, "Bad Request: no data");
                            return;
                        }

                        // save file
                        try(final FileOutputStream fos = new FileOutputStream(PrusaSL1Configuration.getInstance().getFileOutputDirectory()+ part.filename)) {
                            fos.write(part.bytes);
                        }
                        catch(final Exception e) {
                            reply(httpExchange, 500, "Internal Server Error: " + e.getMessage());
                            return;
                        }

                        System.out.println("Got new File: " + PrusaSL1Configuration.getInstance().getFileOutputDirectory() + part.filename);

                        reply(httpExchange, 200,
                                "{\n" +
                                        "  \"files\": {\n" +
                                        "    \"local\": {\n" +
                                        "      \"name\": \""+part.filename+"\",\n" +
                                        "      \"path\": \""+part.filename+"\",\n" +
                                        "      \"type\": \"machinecode\",\n" +
                                        "      \"typePath\": [\"machinecode\", \"gcode\"],\n" +
                                        "      \"origin\": \"local\",\n" +
                                        "      \"refs\": {}\n" +
                                        "    }\n" +
                                        "  },\n" +
                                        "  \"done\": false\n" +
                                        "}");

                        if(null != getEventListener())
                            getEventListener().onNewFile(new PrintHostTemporaryFile(PrusaSL1Configuration.getInstance().getFileOutputDirectory() + part.filename));

                        return;
                    }

                    reply(httpExchange, 400, "Bad Request: no sl1 file in request");
                }
            });

            server.start();

            setMessage("Print Host is running on port " + PrusaSL1Configuration.getInstance().getPrusaSL1ServerPort());
        }
        catch (final Throwable tr) {
            stop();
            setMessage("Error starting Print Host: " + tr);
        }
    }

    public synchronized void stop() {
        try {
            if(null != server)
                server.stop(0);
            setMessage("Print Host stopped");
        }
        catch(final Throwable tr) {
            setMessage("Error terminating Server: " + tr);
        }
        finally {
            server = null;
        }
    }

    public synchronized void restart() {
        if(isRunning()) {
            stop();
            start();
        }
    }

    public synchronized boolean isRunning() {
        return null != server;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void reply(final HttpExchange httpExchange, final int state, final String data) throws IOException {
        final byte response[] = data.getBytes("UTF-8");

        httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        httpExchange.sendResponseHeaders(200, response.length);

        final OutputStream out = httpExchange.getResponseBody();
        out.write(response);
        out.close();
    }
}
