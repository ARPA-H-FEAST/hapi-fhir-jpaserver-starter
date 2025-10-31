package ca.uhn.fhir.jpa.starter.feastutil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class oauthCaller {

    public Logger ourLogger = LoggerFactory.getLogger(oauthCaller.class);

    public boolean verifyToken(String token) throws IOException, InterruptedException {

        String authUrl = "http://localhost:8000/fhir-api/oauth/userinfo/";
        
        // BodyPublisher bp = BodyPublishers.ofString("{grant_type: client_credentials}");
        // ourLogger.info("===> Posted body: " + bp.toString());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(authUrl))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + token)
            // .POST(bp)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ourLogger.info("Response Code: " + response.statusCode());
        ourLogger.info("Response Body: " + response.body());
        
        ObjectMapper mapper = new ObjectMapper();
        // XXX FIXME
        authResponse response_map = mapper.readValue(response.body(), authResponse.class);
        ourLogger.info("Built response map: " + response_map.toString());
        Boolean authorized = response_map.sub != null ? true : false;

        return authorized;

    } 
}
