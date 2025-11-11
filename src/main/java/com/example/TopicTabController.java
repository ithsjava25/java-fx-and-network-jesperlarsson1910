package com.example;

import javafx.application.Platform;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.http.HttpClient;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class TopicTabController {

    private TopicTabModel topicTabModel;

    @FXML
    public ListView<NtfyMessage> messageView;

    @FXML
    private void initialize(String newTopic, String HOSTNAME, HttpClient httpClient) {
        topicTabModel = new TopicTabModel(newTopic, HOSTNAME, httpClient);
        messageView.setItems(topicTabModel.getMessageHistory());
    }


}
