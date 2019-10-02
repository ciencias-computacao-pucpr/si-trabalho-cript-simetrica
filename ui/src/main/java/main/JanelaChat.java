package main;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JanelaChat extends VBox {
    private final ListView<Mensagem> mensagens;
    private final TextField mensagem;

    private final TextField nome;
    private final TextField ip;

    private final Button btnEnviar;
    private final TextField portaEsculta;
    private Socket socket;

    public JanelaChat() {
        portaEsculta = new TextField();
        portaEsculta.setEditable(false);
        nome = new TextField();
        ip = new TextField("127.0.0.1:1234");

        iniciarServer();

        Label labelPortaEscuta = new Label("Porta escuta:");
        Label labelNome = new Label("Nome:");
        Label ipLabel = new Label("IP:");

        labelPortaEscuta.setPrefWidth(100);
        labelNome.setPrefWidth(100);
        ipLabel.setPrefWidth(100);

//        Button iniciarEsculta = new Button("Iniciar esculta");

//        iniciarEsculta.setOnAction(this::iniciarServer);

        getChildren().add(new VBox(
                new HBox(labelPortaEscuta, portaEsculta),
                new HBox(labelNome, nome),
                new HBox(ipLabel, ip)
        ));

        setSpacing(5.0);
        mensagens = new ListView<>();
        mensagens.getItems().addAll(IntStream.range(0, 100).mapToObj(i -> (Mensagem) null).collect(Collectors.toList()));
        mensagens.scrollTo(Integer.MAX_VALUE);
        mensagens.setCellFactory(stringListView -> {
            ListCell<Mensagem> listCell = new ListCell<>() {

                @Override
                protected void updateItem(Mensagem mensagem, boolean empty) {
                    setMaxWidth(getListView().getWidth());
                    setMaxHeight(Double.MAX_VALUE);
                    setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                    super.updateItem(mensagem, empty);
                    if (mensagem == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        HBox wrapper = new HBox(5.0);
                        wrapper.maxWidth(getListView().getWidth());
                        if (mensagem.recebida)
                            wrapper.setAlignment(Pos.BASELINE_LEFT);
                        else
                            wrapper.setAlignment(Pos.BASELINE_RIGHT);

                        Label lbl = new Label(mensagem.texto);

                        lbl.setPadding(new Insets(5.0));
                        lbl.setTextFill(Color.WHITE);
                        lbl.setWrapText(true);
                        lbl.maxWidth(this.getListView().getWidth());
                        lbl.prefWidth(this.getListView().getWidth());
                        CornerRadii rdx = new CornerRadii(10);
                        if (mensagem.recebida)
                            lbl.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, rdx, Insets.EMPTY)));
                        else {
                            lbl.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE, rdx, Insets.EMPTY)));

                        }
                        wrapper.setMaxHeight(Double.MAX_VALUE);
                        wrapper.getChildren().add(lbl);
                        setGraphic(wrapper);
                    }
                }
            };
            listCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return listCell;
        });
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

    private void iniciarServer() {
        try {
            ServerSocket server = new ServerSocket(0);
            Optional<InetAddress> first = NetworkInterface.networkInterfaces().map(ni -> {
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress ia = inetAddresses.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
                return null;
            })
                    .findFirst();
            first.ifPresent(ia -> portaEsculta.setText(ia.getHostAddress() + ":" + server.getLocalPort()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Socket socket = server.accept();
                            ObjectInputStream obj = new ObjectInputStream(socket.getInputStream());
                            Mensagem msg = (Mensagem) obj.readObject();
                            Platform.runLater(() -> mensagens.getItems().add(msg));
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        Platform.runLater(() -> showError(e));
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void monitorarSocket(final Socket socket) {
        new Thread(() -> {
            try (ObjectInputStream obj = new ObjectInputStream(socket.getInputStream())) {
                while (true) {
                    final Mensagem msg = (Mensagem) obj.readObject();
                    Platform.runLater(() -> mensagens.getItems().add(msg));
                    Thread.sleep(1000);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError(e));
            }
        }).start();
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage(), ButtonType.CLOSE);
        alert.setResizable(true);
        alert.setWidth(300);
        alert.setHeight(200);
        alert.show();
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onEnviarClick(new ActionEvent(keyEvent.getSource(), keyEvent.getTarget()));
        }
    }

    private void mensagemAdded(ObservableValue<? extends ObservableList<Mensagem>> observableValue, ObservableList<Mensagem> strings, ObservableList<Mensagem> t1) {
        mensagens.scrollTo(mensagens.getItems().get(mensagens.getItems().size() - 1));
    }

    private void onEnviarClick(ActionEvent evt) {
        String textoMensagem = mensagem.getText();
        if (!textoMensagem.trim().isEmpty()) {
            try {
                Mensagem mensagem = new Mensagem(textoMensagem, true, nome.getText());
                enviarMensagem(mensagem);
                mensagens.getItems().add(new Mensagem(textoMensagem, false, nome.getText()));
                mensagens.scrollTo(Integer.MAX_VALUE);
                this.mensagem.setText("");
            } catch (IOException e) {
                mensagens.getItems().add(new Mensagem(textoMensagem + "(" + e.getMessage() + ")", false, nome.getText()));
                mensagem.setText("");
            }
        }
    }

    private void enviarMensagem(Mensagem mensagem) throws IOException {
        String[] ipPorta = ip.getText().trim().split(":");
        socket = new Socket(ipPorta[0], Integer.parseInt(ipPorta[1]));

        try (OutputStream outputStream = socket.getOutputStream()) {
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(mensagem);
            os.flush();
        } catch (Exception e) {
            showError(e);
        }

    }

    public static class Mensagem implements Serializable {
        public final String texto;
        public final boolean recebida;
        public final String nome;

        public Mensagem(String texto, boolean recebida, String nome) {
            this.texto = texto;
            this.recebida = recebida;
            this.nome = nome;
        }
    }
}
