import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        Usuario valdemar = new Usuario("Valdemar", "chavedovaldemar.");
        Usuario jose = new Usuario("José", "chaveninguemsabe");

        valdemar.enviarMensagem(jose, "Olá");
        jose.enviarMensagem(valdemar, "Como vai você?");
        valdemar.enviarMensagem(jose, "Vou bem! Está curtindo esta conversa secreta?");
        jose.enviarMensagem(valdemar, "Não muito, estou sem assunto.");
    }
}
