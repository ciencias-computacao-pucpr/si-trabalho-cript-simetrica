import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KDC {
    public static KDC getInstancia() {
        if (instancia == null){
            instancia = new KDC();
        }
        return instancia;
    }

    public void adicionarChave(String nome, String chave) {
        chaves.put(nome, criarChave(chave));
    }

    public byte[] iniciarSessao(String origem, byte[] destinoCifrado) {
        String destino = validarEDescriptografarMensagem(origem, destinoCifrado);

        if (!chaves.containsKey(destino)) {
            throw new IllegalArgumentException("Destino desconhecido");
        }

        Cifrador cifrador = new Cifrador(chaves.get(origem));
        if (sessoes.containsKey(origem)) {
            Sessao sessao = sessoes.get(origem);
            if (!sessao.isExpirada()) {
                return cifrador.cifrar(origem+sessao.getChave());
            }
        }

        String segredoChave = UUID.randomUUID().toString().substring(0,16);
        Sessao sessao = new Sessao(segredoChave);
        sessoes.put(origem, sessao);

        return cifrador.cifrar(origem+segredoChave);
    }

    public byte[] getSessaoCom(String nome, byte[] nomeUsuarioSessaoCifrado) {
        String nomeUsuarioSessaoDecifrado = validarEDescriptografarMensagem(nome, nomeUsuarioSessaoCifrado);

        if (!sessoes.containsKey(nomeUsuarioSessaoDecifrado) || sessoes.get(nomeUsuarioSessaoDecifrado).isExpirada()) {
            throw new IllegalArgumentException("Sessão não existe ou expirou");
        }
        return concatenarECifrar(nome, sessoes.get(nomeUsuarioSessaoDecifrado).getChave());
    }

    private static KDC instancia;

    private final Map<String, Key> chaves;
    private final Map<String, Sessao> sessoes;

    private KDC() {
        this.chaves = new HashMap<>();
        this.sessoes = new HashMap<>();
    }

    private SecretKeySpec criarChave(String chave) {
        return new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private byte[] getChaveSessao() {
        return new byte[0];
    }

    private byte[] concatenarECifrar(String nome, String conteudo) {
        return new Cifrador(chaves.get(nome)).cifrar(nome+conteudo);
    }

    private String validarEDescriptografarMensagem(String nome, byte[] conteudo) {
        if (!chaves.containsKey(nome)) {
            throw new IllegalArgumentException("Origem desconhecido");
        }

        Cifrador cifrador = new Cifrador(chaves.get(nome));
        String destinoDecifrado = cifrador.decifrar(conteudo);
        if (!destinoDecifrado.startsWith(nome)) {
            throw new IllegalArgumentException("Texto de destino não pode ser decriptografado");
        }

        return destinoDecifrado.substring(nome.length());
    }
}
