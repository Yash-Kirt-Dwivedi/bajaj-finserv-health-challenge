package com.bajaj.challenge_app;

import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class StartupRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void runOnStartup() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            String webhookUrl = (String) responseBody.get("webhook");
            String accessToken = (String) responseBody.get("accessToken");

            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            Object usersObj = data.get("users");

            if (usersObj instanceof List) {
                // ✅ Handle Question 1 only
                List<Map<String, Object>> users = (List<Map<String, Object>>) usersObj;

                Map<String, Object> result = new HashMap<>();
                result.put("regNo", "REG12347");
                result.put("outcome", solveMutualFollowers(users));

                HttpHeaders webhookHeaders = new HttpHeaders();
                webhookHeaders.setContentType(MediaType.APPLICATION_JSON);
                webhookHeaders.set("Authorization", accessToken);

                HttpEntity<Map<String, Object>> webhookRequest = new HttpEntity<>(result, webhookHeaders);

                for (int i = 0; i < 4; i++) {
                    try {
                        restTemplate.postForEntity(webhookUrl, webhookRequest, String.class);
                        System.out.println("✅ Sent result to webhook!");
                        break;
                    } catch (Exception e) {
                        System.out.println("❌ Attempt " + (i + 1) + " failed. Retrying...");
                        Thread.sleep(1000);
                    }
                }
            } else {
                System.out.println("❌ Unexpected format: users is not a list. This might be Question 2.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<List<Integer>> solveMutualFollowers(List<Map<String, Object>> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, List<Integer>> followMap = new HashMap<>();

        for (Map<String, Object> user : users) {
            int id = (int) user.get("id");
            List<Integer> follows = (List<Integer>) user.get("follows ");
            followMap.put(id, follows);
        }

        Set<String> seen = new HashSet<>();

        for (int userId : followMap.keySet()) {
            for (int followedId : followMap.get(userId)) {
                if (followMap.containsKey(followedId) && followMap.get(followedId).contains(userId)) {
                    int min = Math.min(userId, followedId);
                    int max = Math.max(userId, followedId);
                    String key = min + "-" + max;
                    if (!seen.contains(key)) {
                        result.add(Arrays.asList(min, max));
                        seen.add(key);
                    }
                }
            }
        }

        return result;
    }
}
