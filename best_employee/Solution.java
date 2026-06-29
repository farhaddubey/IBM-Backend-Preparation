import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Solution {

    public static String bestEmployee(String company) {

        // Return -1 if not found
        String answer = "-1";

        double bestAvg = -1.0;
        int bestProjectCount = -1;

        try {
            HttpClient client = HttpClient.newHttpClient();

            // URL Encoding
            String encodedCompany = URLEncoder.encode(company, StandardCharsets.UTF_8);

            // First Page is passed as starting 1
            String url = "https://jsonmock.hackerrank.com/api/companies" + "?company=" + encodedCompany + "&page=1";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.body());

            int totalPages = ((Long) json.get("total_pages")).intValue();

            // Traversing all pages
            for (int page = 1; page <= totalPages; page++) {

                String url = "https://jsonmock.hackerrank.com/api/companies" + "?company=" + encodedCompany + "&page="
                        + page;
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject root = (JSONObject) parser.parse(response.body());
                JSONArray offices = (JSONArray) root.get("data");

                if (offices == null || offices.isEmpty()) {
                    continue;
                }

                // Offices

                for (Object officeObj : offices) {
                    JSONObject office = (JSONObject) officeObj;
                    JSONArray employees = (JSONArray) office.get("employees");

                    if (employees == null || employees.isEmpty()) {
                        continue;
                    }

                    // Exploring employees
                    for (Object emplObj : employees) {
                        JSONObject employee = (JSONObject) emplObj;
                        Object idObj = employee.get("id");
                        if (idObj == null) {
                            continue;
                        }

                        String employeeId = idObj.toString().trim();
                        JSONArray projects = (JSONArray) employee.get("projects");

                        if (projects == null || projects.isEmpty()) {
                            continue;
                        }

                        double totalScore = 0;

                        // Projects
                        for (Object projectObj : projects) {
                            JSONObject project = (JSONObject) projectObj;

                            Object scoreObj = project.get("score");
                            if (scoreObj == null) {
                                continue;
                            }

                            double score = ((Number) scoreObj).doubleValue();
                            totalScore += score;
                        }

                        // Average
                        double avg = totalScore / projects.size();
                        int projectCount = projects.size();

                        // Better avg
                        if (avg > bestAvg) {
                            bestAvg = avg;
                            bestProjectCount = projectCount;
                            answer = employeeId;
                        }

                        // Same average
                        else if (Double.compare(avg, bestAvg) == 0) {
                            // More projects win
                            if (projectCount > bestProjectCount) {
                                bestProjectCount = projectCount;
                                answer = employeeId;
                            }

                            // Same project count
                            else if (projectCount == bestProjectCount) {
                                // Lexicographically smaller id
                                if (employeeId.compareTo(answer) < 0) {
                                    answer = employeeId;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }

        return answer;
    }
}