import java.time.Duration;
import java.time.Instant;

public class Sessao {
    private String chave;
    private Instant validade;
    private Cifrador cifrador;

    public Sessao(String chave) {
        this.chave = chave;
        this.validade = Instant.now().plus(Duration.ofHours(1));
        this.cifrador = new Cifrador(chave);
    }

    public String getChave() {
        if (isExpirada()) {
            throw new RuntimeException("Sessão expirada");
        }

        return chave;
    }

    public boolean isExpirada() {
        return Instant.now().isAfter(validade);
    }

    public byte[] cifrar(String conteudo) {
        return cifrador.cifrar(conteudo);
    }

    public String decifrar(byte[] conteudo) {
        return cifrador.decifrar(conteudo);
    }
}
