package com.example;

import javafx.application.Platform;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.http.HttpClient;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();
    private TabPane tabPane;

    @FXML
    private TextField input;
    @FXML
    private MenuItem addTopic;



    private ObservableList<TopicTabController> topicTabControllers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tabPane = new TopicTabModel(model.getHttpClient(), model.getHostname());


    }

    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage(tabPane.getSelectionModel(),input.getText().trim());
        input.clear();
    }

    public void addTopic(ActionEvent actionEvent) {
        TopicTabController topicTabController = new TopicTabController();
        topicTabControllers.add(topicTabController);


    }



}
