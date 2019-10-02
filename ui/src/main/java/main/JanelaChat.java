package main;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class JanelaChat extends VBox {
    private final ListView<String> mensagens;
    private final TextField mensagem;

    private final TextField nome;
    private final TextField ip;

    private final Button btnEnviar;
    private final TextField portaEsculta;

    public JanelaChat() {
        portaEsculta = new TextField("1234");
        nome = new TextField();
        ip = new TextField("127.0.0.1:1234");

        Label labelPortaEscuta = new Label("Porta escuta:");
        Label labelNome = new Label("Nome:");
        Label ipLabel = new Label("IP:");

        labelPortaEscuta.setPrefWidth(100);
        labelNome.setPrefWidth(100);
        ipLabel.setPrefWidth(100);

        Button iniciarEsculta = new Button("Iniciar esculta");

        iniciarEsculta.setOnAction(this::iniciarServer);

        getChildren().add(new VBox(
                new HBox(labelPortaEscuta, portaEsculta, iniciarEsculta),
                new HBox(labelNome, nome),
                new HBox(ipLabel, ip)
        ));

        setSpacing(5.0);
        mensagens = new ListView<>();
        getChildren().add(mensagens);

        mensagem = new TextField();
        mensagem.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(mensagem, Priority.ALWAYS);
        btnEnviar = new Button("Enviar");
        btnEnviar.setOnAction(this::onEnviarClick);
        mensagem.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                JanelaChat.this.onKeyPressed(keyEvent);
            }
        });
        getChildren().add(new HBox(mensagem, btnEnviar));

        mensagens.itemsProperty().addListener(this::mensagemAdded);
    }

    private void iniciarServer(ActionEvent actionEvent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(Integer.parseInt(portaEsculta.getText()));
                    while (true) {
                        Socket socket = server.accept();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String s = reader.readLine();
//                        String[] split = s.split(":");
                        Platform.runLater(() -> mensagens.getItems().add(s));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
                        alert.setResizable(true);
                        alert.setWidth(300);
                        alert.setHeight(200);
                        alert.show();
                    });
                }
            }
        }).start();
    }

    private void mensagemAdded(ObservableValue<? extends ObservableList<String>> observableValue, ObservableList<String> strings, ObservableList<String> t1) {
        mensagens.scrollTo(mensagens.getItems().size());
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onEnviarClick(new ActionEvent(keyEvent.getSource(), keyEvent.getTarget()));
        }
    }


    private void onEnviarClick(ActionEvent evt) {
        String textoMensagem = mensagem.getText();
        if (!textoMensagem.trim().isEmpty()) {
            try {
                enviarMensagem(textoMensagem);
                mensagens.getItems().add("Eu:" + textoMensagem);
                mensagem.setText("");
            } catch (IOException e) {
                mensagens.getItems().add(textoMensagem + " (" + e.getMessage() + ")");
                mensagem.setText("");
            }
        }
    }

    private void enviarMensagem(String mensagem) throws IOException {
        String[] ipPorta = ip.getText().trim().split(":");
        Socket socket = new Socket(ipPorta[0], Integer.parseInt(ipPorta[1]));

        try (OutputStream outputStream = socket.getOutputStream();) {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write(nome.getText() + ":" + mensagem + System.lineSeparator());
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
