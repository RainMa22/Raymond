package me.rainma22;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DownloaderImpl extends Downloader {
    private static final String USER_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0";

    public DownloaderImpl() {
    }


//    public static void main(String[] args) throws IOException, ReCaptchaException {
//        DownloaderImpl downloader = new DownloaderImpl();
//        downloader.execute(new URL("https://www.youtube.com/watch?v=9lNZ_Rnr7Jc"), "GET", null, null);
//    }

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException, UnsupportedEncodingException {
        // Create an instance of HttpClient.
        return execute(new URL(request.url()), request.httpMethod(), request.dataToSend(), request.headers());
    }

    public Response execute(URL url, String method, byte[] dataToSend, Map<String, List<String>> header) throws IOException, ReCaptchaException, UnsupportedEncodingException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setDoInput(true);
        connection.setRequestMethod(method);
        if (header != null) {
            header.forEach((key, values) -> {
                values.forEach((value) -> {
                    connection.addRequestProperty(key, value);
                });
            });
        }
        if (dataToSend != null) {
            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(dataToSend);
            out.flush();
            out.close();
        }
        int status = connection.getResponseCode();

        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM) {
            String location = connection.getHeaderField("Location");
            URL newUrl = new URL(location);
            return execute(newUrl, method, dataToSend, header);
        }

        InputStream responseStream = connection.getInputStream();


        byte[] in = responseStream.readAllBytes();


        String html = new String(in);


        Response response = new Response(status, connection.getResponseMessage(),
                connection.getHeaderFields(), html, url.toString());

        return response;
    }
}