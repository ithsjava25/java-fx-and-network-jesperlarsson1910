package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javafx.scene.control.*;

public class TopicTabModel extends Tab {
    private final SimpleStringProperty topic;

    private final HttpClient httpClient;
    private final String HOSTNAME;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();

    public TopicTabModel(String newTopic, String HOSTNAME, HttpClient httpClient) {
        this.topic = new SimpleStringProperty(newTopic);
        this.HOSTNAME = HOSTNAME;
        this.httpClient = httpClient;

        super.setText(topic.getValue());

        receiveMessage();
    }

    public ObservableList<NtfyMessage> getMessageHistory() {
        return messageHistory;
    }

    public String getTopic() {
        return topic.get();
    }

    public SimpleStringProperty topicProperty() {
        return topic;
    }

    public void changeTopic(String newtopic) {
        Platform.runLater(() -> topic.set(newtopic.replaceAll("^\\/\\w+\\s", "")));
        Platform.runLater(() -> receiveMessage());

    }

    public void receiveMessage() {
        Platform.runLater(() -> messageHistory.clear());

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOSTNAME + "/" + topic.getValue() + "/json?since=all"))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> objectMapper.readValue(s, NtfyMessage.class))
                        .filter(m -> m.event().equals("message"))
                        .forEach((m -> Platform.runLater(() -> messageHistory.add(m)))));
    }
}
