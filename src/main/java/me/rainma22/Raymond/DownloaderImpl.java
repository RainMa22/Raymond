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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class DownloaderImpl extends Downloader {

    private static Map<String, List<String>> downloadHeader;

    static {
        downloadHeader = new HashMap<>();
//        downloadHeader = Map.of("range", List.of("0-"));
    }

    private static final List<String> USER_AGENTS
            = List.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0");

    public DownloaderImpl() {
    }

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
        String ua = USER_AGENTS.get(RandomUtils.insecure().randomInt(0, USER_AGENTS.size()));
        connection.addRequestProperty("User-Agent", ua);
        connection.setRequestMethod(method);
        HashMap<String, List<String>> newHeader = new HashMap<>();
        if (header != null) {
            newHeader.putAll(header);
        }
        newHeader.putAll(downloadHeader);
        newHeader.forEach((key, values) -> {
            values.forEach((value) -> {
                connection.addRequestProperty(key, value);
            });
        });
        if(!connection.getRequestProperties().containsKey("si")){
            connection.addRequestProperty("si", RandomStringUtils.insecure().nextAlphanumeric(16));
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
