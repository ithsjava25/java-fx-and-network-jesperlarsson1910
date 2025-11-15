package com.example;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    SimpleStringProperty topic = new SimpleStringProperty();
    Consumer<NtfyMessage> messageHandler;

    String message;

    @Override
    public SimpleStringProperty topicProperty() {
        return topic;
    }

    @Override
    public String getTopic() {
        return topic.get();
    }

    @Override
    public void setTopic(String topic) {
        this.topic.set(topic);
    }

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void recieve(Consumer<NtfyMessage> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public boolean sendFile(File attachment) {
        return false;
    }
}
