import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.MalformedURLException;

public class Main {

    private static final String API_KEY = "Ie3U14twglGe9TvDJo4dEVDDdh1fimYNk1u5Li5q";
    private static final String REMOTE_SERVICE_URL =
            "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        NasaAnswer nasaAnswer = getNasaAnswer(REMOTE_SERVICE_URL, httpClient);
        if (nasaAnswerIsNull(nasaAnswer)) return;
        writeFile(nasaAnswer.getUrl(), httpClient);
    }

    private static void writeFile(String fileUrl, CloseableHttpClient httpClient) throws MalformedURLException {
        String fileName = new File(fileUrl).getName();
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                System.out.println(file.createNewFile());
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpGet request = new HttpGet(fileUrl);
        if (file.exists() && file.canWrite()) {
            try (CloseableHttpResponse response = httpClient.execute(request);
                 BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))
            ) {
                int inByte;
                while ((inByte = in.read()) != -1) {
                    out.write(inByte);
                }
                out.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static NasaAnswer getNasaAnswer(String url, CloseableHttpClient httpClient) {
        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return MAPPER.readValue(response.getEntity().getContent(), new TypeReference<>() {
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static boolean nasaAnswerIsNull(NasaAnswer response) {
        if (response == null) {
            System.out.println("Server answer is absent");
            return true;
        } else {
            return false;
        }
    }
}