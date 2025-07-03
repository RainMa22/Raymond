package me.rainma22.Raymond;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloaderImpl extends Downloader {
    private static Map<String, List<String>> downloadHeader;

    static {
        downloadHeader = new HashMap<>();
        downloadHeader = Map.of("range", List.of("0-"));
    }

    private static final String USER_AGENT
            = "Mozilla/5.0 (X11; U; Linux; rv:132.0esr) Gecko/20161309 Firefox/132.0esr";

    public DownloaderImpl() {
    }


//    public static void main(String[] args) throws IOException, ReCaptchaException {
//        DownloaderImpl downloader = new DownloaderImpl();
//        downloader.execute(new URL("https://www.youtube.com/watch?v=9lNZ_Rnr7Jc"), "GET", null, null);
//    }

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException, UnsupportedEncodingException {
        // Create an instance of HttpClient.
        URL target;
        try {
            target = new URI(request.url()).toURL();
        } catch (URISyntaxException e) {
            MalformedURLException mue = new MalformedURLException();
            mue.addSuppressed(e);
            throw mue;
        }
        return execute(target, request.httpMethod(), request.dataToSend(), request.headers());
    }

    public Response execute(URL url, String method, byte[] dataToSend, Map<String, List<String>> header) throws IOException, ReCaptchaException, UnsupportedEncodingException {

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.addRequestProperty("User-Agent", USER_AGENT);
//        connection.setDoInput(true);
        connection.setRequestMethod(method);
        HashMap<String, List<String>> newHeader= new HashMap<>();
        if (header != null) newHeader.putAll(header);
        newHeader.putAll(downloadHeader);
        newHeader.forEach((key, values) -> {
            values.forEach((value) -> {
                connection.addRequestProperty(key, value);
            });
        });
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
            URL newUrl;
            try {
                newUrl = new URI(location).toURL();
            } catch (URISyntaxException e) {
                MalformedURLException mue = new MalformedURLException();
                mue.addSuppressed(e);
                throw mue;
            }
            return execute(newUrl, method, dataToSend, header);
        }

        InputStream responseStream = connection.getInputStream();


        byte[] in = responseStream.readAllBytes();


        String dataString = new String(in);


        Response response = new Response(status, connection.getResponseMessage(),
                connection.getHeaderFields(), dataString, url.toString());

        return response;
    }
}