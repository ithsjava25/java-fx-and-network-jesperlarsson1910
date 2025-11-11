package com.example;

import javafx.application.Platform;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    private TabPane tabPane;
    @FXML
    private TextField input;

    @FXML
    private void initialize() {
        if(tabPane.getTabs().size() == 0) {
            model.addTopic(tabPane);
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage(tabPane.getSelectionModel(),input.getText().trim());
        input.clear();
    }

    public void addTopic(ActionEvent actionEvent) {
        model.addTopic(tabPane);
    }



}
