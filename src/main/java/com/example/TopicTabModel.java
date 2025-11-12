package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import javafx.scene.control.*;

public class TopicTabModel extends TabPane {
    private final HttpClient httpClient;
    private final String HOSTNAME;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();

    public TopicTabModel(HttpClient httpClient, String HOSTNAME) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.HOSTNAME = Objects.requireNonNull(HOSTNAME);

        receiveMessage();
    }



    public ObservableList<NtfyMessage> getMessageHistory() {
        return messageHistory;
    }

    public void changeTopic(String newtopic) {
        //Platform.runLater(() -> topic.set(newtopic.replaceAll("^\\/\\w+\\s", "")));
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

    public void addTopic(TabPane tabPane, TopicTabController topicTabController) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Add Topic");

        ButtonType addTopicButton = new ButtonType("Add Topic", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addTopicButton, ButtonType.CANCEL);

        TextField topic = new TextField();
        topic.setPromptText("Topic");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(topic, 0, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(() -> topic.requestFocus());

        dialog.setResultConverter(pressedButton -> {
            if (pressedButton == addTopicButton) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setController(TopicTabController.class);
                    AnchorPane anchorPane= loader.load(getClass().getResource("tab-view.fxml"));

                    tabPane.getTabs().add(topicTabController.getTopicTabModel());
                    topicTabController.getTopicTabModel().setContent(anchorPane);

                    topicTabController.superSetter(topic.getText(), HOSTNAME, httpClient);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });

        dialog.show();
    }
}
