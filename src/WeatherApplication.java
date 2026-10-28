import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WeatherApplication {

    private static final String API_URL =
            "https://api.weather.yandex.ru/v2/forecast";


    public static void main(String[] args) throws Exception {
        String apiKey = "3641df7a-941e-4f8c-9220-b068c9c796e2";


        double lat = 55.75;
        double lon = 37.62;
        int limit = 5;

        String url = API_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit + "&lang=ru_RU";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("X-Yandex-Weather-Key", apiKey).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Ошибка API: " + response.statusCode());
            System.err.println(response.body());
            return;
        }

        String json = response.body();

        System.out.println("Полный JSON ответ:");
        System.out.println(json);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode tempNode = root.path("fact").path("temp");

        if (!tempNode.isNumber()) {
            System.err.println("Текущая температура не найдена.");
            return;
        }

        System.out.println("\nТекущая температура: " + tempNode.asInt() + " °C");

        JsonNode forecasts = root.path("forecasts");
        List<Integer> temperatures = new ArrayList<>();

        int days = Math.min(limit, forecasts.size());

        for (int i = 0; i < days; i++) {
            JsonNode tempAvg = forecasts.get(i)
                    .path("parts")
                    .path("day")
                    .path("temp_avg");

            if (tempAvg.isNumber()) {
                temperatures.add(tempAvg.asInt());
            }
        }

        if (temperatures.isEmpty()) {
            System.out.println("Нет данных.");
            return;
        }

        double sum = 0;

        for (int temperature : temperatures) {
            sum += temperature;
        }

        double average = sum / temperatures.size();

        System.out.printf("Средняя дневная температура за %d суток: %.2f °C%n", temperatures.size(), average);
    }
}