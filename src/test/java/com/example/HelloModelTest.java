package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@WireMockTest
public class HelloModelTest {

    @BeforeAll
    static void initJfxRuntime() {
        Platform.startup(() -> {});
    }

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        //Act
        model.sendMessage("Hello World");

        //Assert
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wireMockRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);

        stubFor(post("JUV25D").willReturn(ok()));

        model.sendMessage("Hello World");

        //verify
        verify(postRequestedFor(urlEqualTo("/JUV25D")).withRequestBody(containing("Hello World")));

    }

    @Test
    @DisplayName("Recieve messages from server  when called")
    void receiveMessageFromFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        var connection = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());

        CompletableFuture<NtfyMessage> receivedMessageFuture = new CompletableFuture<>();

        String responseBody = "{\"id\":\"testID\",\"time\":1234567890,\"event\":\"message\",\"topic\":\"JUV25D\",\"message\":\"Test\"}\n";

        stubFor(get("/JUV25D/json?since=all")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)
                        .withHeader("Content-Type", "application/json")));

        connection.recieve(receivedMessageFuture::complete);

        NtfyMessage response = receivedMessageFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);

        verify(getRequestedFor(urlEqualTo("/JUV25D/json?since=all")));

        assertThat(response).isNotNull();
        assertThat(response.event()).isEqualTo("message");
        assertThat(response.topic()).isEqualTo("JUV25D");
        assertThat(response.message()).isEqualTo("Test");
    }


    @Test
    @DisplayName("Handle basic test message, image attachment and other attachment separately")
    public void logMessagesFormatsCorrectlyBasedOnContent() {
        var con = new NtfyConnectionImpl();
        var model = new HelloModel(con);

        //basic message
        NtfyMessage message1  = new NtfyMessage("message1", 1234567890L, "message", "testTopic", "test1", null);


        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date timeStamp1 = new Date(message1.time()*1000);
        String formatedMessage1 = dateFormat.format(timeStamp1) + " : " + message1.message();

        //message with image attachment
        String mandrillURL = "https://www.researchgate.net/publication/259521525/figure/fig9/AS:268029699293189@1440914668284/Original-standard-test-image-of-Mandrill-also-known-as-Baboon.png";
        HashMap<String,String> attachment1 = new HashMap<>();
        attachment1.put("name", "mandrill");
        attachment1.put("url", mandrillURL);
        attachment1.put("type", "image/png");

        NtfyMessage message2 = new NtfyMessage("message2", 1234567891L, "message", "testTopic", "test2", attachment1);

        ImageView testImage = new ImageView(new Image(mandrillURL));
        testImage.setPreserveRatio(true);
        testImage.setFitHeight ( 250 );
        testImage.setFitWidth ( 250 );

        Date timeStamp2 = new Date(message2.time()*1000);
        String formatedMessage2 = dateFormat.format(timeStamp2) + " : " + message2.message();

        //message with non-image attachment
        HashMap<String,String> attachment2 = new HashMap<>();
        attachment2.put("name", "mandrill");
        attachment2.put("url", mandrillURL);
        attachment2.put("type", "application/json");

        NtfyMessage message3 = new NtfyMessage("message3", 1234567892L, "message", "testTopic", "test3", attachment2);

        Hyperlink testLink = new Hyperlink(mandrillURL);

        Date timeStamp3 = new Date(message3.time()*1000);
        String formatedMessage3 = dateFormat.format(timeStamp3) + " : " + message3.message();


        model.logMessage(message1);
        model.logMessage(message2);
        model.logMessage(message3);

        //check that messageHistory has the raw messages
        assertThat(model.getMessageHistory().stream().toList()).contains(message1, message2, message3);
        //check that formatedMessages contains correctly formated strings and objects
        assertThat(model.getFormattedMessages().stream().toList()).contains(formatedMessage1, formatedMessage2, formatedMessage3);
        assertThat(model.getFormattedMessages().stream().toList()).extracting(Object::getClass).contains(ImageView.class, Hyperlink.class);
    }

}
