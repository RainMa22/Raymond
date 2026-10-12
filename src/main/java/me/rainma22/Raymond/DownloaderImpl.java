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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.rainma22.Raymond.Debug.Debugger;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class DownloaderImpl extends Downloader {
    private HttpClient client;
    private Map<String, List<String>> downloadHeader = new HashMap<>();

    private static final String USER_AGENT
            = "Mozilla/5.0 (X11; Linux x86_64; rv:150.0) Gecko/20100101 Firefox/150.0";

    public DownloaderImpl() {
        var builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(1));
        client = builder
                .build();
    }

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException, UnsupportedEncodingException {
        URL target;
        try {
            target = URI.create(request.url()).toURL();
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException();
            mue.addSuppressed(e);
            throw mue;
        }
        return execute(target, request.httpMethod(), request.dataToSend(), request.headers());
    }

    public Response execute(URL url, String method, byte[] dataToSend, Map<String, List<String>> header) throws IOException, ReCaptchaException, UnsupportedEncodingException {
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(url.toURI());
            reqBuilder.header("User-Agent", USER_AGENT);
            reqBuilder.method(method, dataToSend == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(dataToSend));
            HashMap<String, List<String>> newHeader = new HashMap<>();
            if (header != null) {
                newHeader.putAll(header);
            }
            newHeader.putAll(downloadHeader);
            newHeader.forEach((key, values) -> {
                values.forEach((value) -> {
                    reqBuilder.header(key, value);
                });
            });
            if (!newHeader.containsKey("si")) {
                reqBuilder.header("si", RandomStringUtils.insecure().nextAlphanumeric(16));
            }

            HttpRequest req = reqBuilder.build();
            HttpResponse<String> res;
            res = client.send(req, HttpResponse.BodyHandlers.ofString());
            int status = res.statusCode();
            String dataString = res.body();

            return new Response(status, "",
                    res.headers().map(), dataString, url.toString());
        } catch (URISyntaxException | InterruptedException e) {
            throw new IOException(e);
        }
    }
}
