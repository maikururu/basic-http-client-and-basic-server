package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private int statusCode = 200;
    private final Map<String, String> headers = new HashMap<>();
    private String responseBody;

    public HttpClient(String hostname, int port, String requestTarget) throws IOException {
                /*
       The Http request consist of a request line and zero or more header lines, terminated by a blank line
       The request line consists of a verb (GET, POST) a request target and the HTTP version
        For example "GET /index.html HTML/1.1"
                */
        Socket socket = new Socket(hostname, 80);
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                /*
        A request header consists of name: value
        The Host header is the same as the web browser shows in the menu bar
                */
                "Host: " + hostname + "\r\n\r\n";

        socket.getOutputStream().write(request.getBytes());
        //The first line in the response is called the response line
        String line = readLine(socket);
                /*
        The response line consists of the HTTP version, a response code and a descriptin
        E.G "404 not found"
                */
        System.out.println(line);
        String[] parts = line.toString().split(" ");
        // The status line is the second word in trhe response line
        statusCode = Integer.parseInt(parts[1]);
        // After the response line, the response has zero or more response headers
        String headerLine;
        while(!(headerLine = readLine(socket)).isEmpty()) {
            // Each header consists of name: value
            int colonPos = headerLine.indexOf(':'); // finds the position of the colon(:) in the headerline
            String headerName = headerLine.substring(0, colonPos);
            // Spaces at the beginning and the end of the header value should be ignored
            String headerValue = headerLine.substring(colonPos+1).trim();

            headers.put(headerName, headerValue);
        }
        // The Content-Length header tells us how many bytes are in the response following the headers
        int contentLength = Integer.parseInt(getResponseHeader("Content-Length"));
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            body.append((char)socket.getInputStream().read());
        }
        // The next content-length bytes are called to the response body
        this.responseBody = body.toString();
    }

    private String readLine(Socket socket) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != -1) {
                /*
                Each line is terminated by CRLF (carriage return, line feed)
                or \r\n
                */
            if (c == '\r'){
                socket.getInputStream().read(); //reads the \n after the \r
                break;
            }
            line.append((char)c);
        }
        return line.toString();
    }

    public static void main(String[] args) throws IOException {

        new HttpClient("urlecho.appspot.com", 80, "/echo?status=200&body=Hello%20world!");


    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseHeader(String headerName) {
        return headers.get(headerName);
    }

    public String getResponseBody() {
        return responseBody;
    }
}
