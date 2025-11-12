package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    private final String HOSTNAME;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Pattern topicChange = Pattern.compile("^\\/topic\\s\\w+$", Pattern.CASE_INSENSITIVE);



    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        HOSTNAME = Objects.requireNonNull(dotenv.get("HOSTNAME"));
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getHostname() {
        return HOSTNAME;
    }

    public void sendMessage(SingleSelectionModel<Tab> tab, String message) {
        if(topicChange.matcher(message).matches()) {
            tab.getSelectedItem().setText(message);
        }

        else {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .uri(URI.create(HOSTNAME + "/" + tab.getSelectedItem().getText()))
                    .build();

            try {
                HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                System.err.println("Error sending message");
            } catch (InterruptedException e) {
                System.err.println("Interrupted sending message");
            }
        }
    }
}

