public class Main {
    public static void main(String[] args) {
        KDC kdc = new KDC("AES");

        Usuario jose = kdc.criarUsuario("José", "chaveninguemsabe");
        Usuario valdemar = kdc.criarUsuario("Valdemar", "chavedovaldemar.");

        valdemar.enviarMensagem(jose, "Olá");
        System.out.println();

        jose.enviarMensagem(valdemar, "Como vai você?");
        System.out.println();

        valdemar.enviarMensagem(jose, "Vou bem! Está curtindo esta conversa secreta?");
        System.out.println();

        jose.enviarMensagem(valdemar, "Não muito, estou sem assunto.");
        System.out.println();
    }
}
