package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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

    public void addTopic(TabPane tabPane) {
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
                addTopic(topic.getText().trim(), tabPane);
            }
            return null;
        });

        dialog.show();
    }

    public void addTopic(String topic, TabPane tabPane) {
        TopicTabModel newTopic = new TopicTabModel(topic, HOSTNAME, httpClient);

        try {
            newTopic.setContent(FXMLLoader.load(HelloFX.class.getResource("tab-view.fxml")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tabPane.getTabs().add(newTopic);
    }
}

