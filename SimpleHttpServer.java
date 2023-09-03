import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

class SimpleHttpServer {

    static final int PORT = 8080;

    public static void main(String[] args) {

        Thread goodByeHook = new Thread(() -> System.out.println("\nBye!"));
        Runtime.getRuntime().addShutdownHook(goodByeHook);

        try {

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/hello", new HelloHandler());
            server.start();
            System.out.println("Server listening on port " + PORT);

        } catch (Exception e) {
            System.out.println("Oops! Something went wrong!");
            e.printStackTrace();
        }
    }
}

class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String requestMethod = httpExchange.getRequestMethod();

        switch (requestMethod) {
            case "GET":
                sendResponse(httpExchange, 200, "Hello my friend!\n");
                break;
            case "POST":
                Optional<String> result = collectRequestBody(httpExchange);
                if (result.isPresent()) {
                    sendEmptyResponse(httpExchange, 200);
                    String requestBody = result.get();
                    System.out.println(requestBody);
                } else {
                    sendEmptyResponse(httpExchange, 500);
                }
                break;
            default:
                sendResponse(httpExchange, 400, "Huh?\n");
                break;
        }
    }

    private void sendResponse(HttpExchange httpExchange, int statusCode, String response) throws IOException {
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(statusCode, response.length());
            outputStream.write(response.getBytes());
        }
    }


    private void sendEmptyResponse(HttpExchange httpExchange, int statusCode) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, -1);
    }

    private Optional<String> collectRequestBody(HttpExchange httpExchange) throws IOException {
        Optional<String> result = Optional.empty();
        try (InputStream inputStream = httpExchange.getRequestBody();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            String requestBody = bufferedReader.lines().collect(Collectors.joining("\n"));
            result = Optional.ofNullable(requestBody);
        }
        return result;
    }

}
