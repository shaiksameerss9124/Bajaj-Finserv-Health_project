package com.example.javatask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class JavaTaskApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JavaTaskApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        String name = "Sameer Shaik";
        String regNo = "22BCE9867";
        String email = "sameer@example.com";

        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        String requestJson = String.format(
                "{ \"name\": \"%s\", \"regNo\": \"%s\", \"email\": \"%s\" }",
                name, regNo, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        JsonNode root = mapper.readTree(response.getBody());
        String webhookUrl = root.get("webhook").asText();
        String accessToken = root.get("accessToken").asText();

        System.out.println("Webhook URL: " + webhookUrl);

        int lastTwo = Integer.parseInt(regNo.replaceAll("\\D", "")
                .substring(regNo.replaceAll("\\D", "").length() - 2));

        String finalQuery;
        if (lastTwo % 2 == 1) {
            finalQuery = "SELECT dept_name, COUNT(student_id) AS total_students " +
                         "FROM Department d " +
                         "JOIN Student s ON d.dept_id = s.dept_id " +
                         "GROUP BY dept_name;";
        } else {
            finalQuery = "SELECT s.name, m.name AS mentor_name " +
                         "FROM Student s LEFT JOIN Mentor m ON s.mentor_id = m.id;";
        }

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        String submitJson = String.format("{ \"finalQuery\": \"%s\" }", finalQuery);
        entity = new HttpEntity<>(submitJson, headers);

        ResponseEntity<String> submitResponse =
                restTemplate.postForEntity(webhookUrl, entity, String.class);

        System.out.println("Submission Response: " + submitResponse.getBody());
        System.exit(0);
    }
}
