import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KDC {

    private final Map<String, Cifrador> chaves;
    private final String algoritmo;

    public KDC(String algoritmo) {
        this.algoritmo = algoritmo;
        this.chaves = new HashMap<>();
        this.sessoes = new HashMap<>();
    }

    public Cifrador criarCifrador(String chave) {
        return new Cifrador(chave, algoritmo);
    }

    public Sessao criarSessao(String chave) {
        return new Sessao(chave, algoritmo);
    }

    public Usuario criarUsuario(String nome, String chave) {
        chaves.put(nome, criarCifrador(chave));

        return new Usuario(this, nome, chave);
    }

    public byte[] iniciarSessao(String origem, byte[] destinoCifrado) {
        String destino = validarEDescriptografarMensagem(origem, destinoCifrado);

        if (!chaves.containsKey(destino)) {
            throw new IllegalArgumentException("Destino desconhecido");
        }

        Cifrador cifrador = chaves.get(origem);
        if (sessoes.containsKey(origem)) {
            Sessao sessao = sessoes.get(origem);
            if (!sessao.isExpirada()) {
                return cifrador.cifrar(origem+sessao.getChave());
            }
        }

        String segredoChave = UUID.randomUUID().toString().substring(0,16);
        Sessao sessao = criarSessao(segredoChave);
        sessoes.put(origem, sessao);

        return cifrador.cifrar(origem+segredoChave);
    }

    private final Map<String, Sessao> sessoes;

    public byte[] recuperarSessao(String nome, byte[] nomeUsuarioSessaoCifrado) {
        String nomeUsuarioSessaoDecifrado = validarEDescriptografarMensagem(nome, nomeUsuarioSessaoCifrado);

        if (!sessoes.containsKey(nomeUsuarioSessaoDecifrado) || sessoes.get(nomeUsuarioSessaoDecifrado).isExpirada()) {
            throw new IllegalArgumentException("Sessão não existe ou expirou");
        }
        return concatenarECifrar(nome, sessoes.get(nomeUsuarioSessaoDecifrado).getChave());
    }

    private byte[] concatenarECifrar(String nome, String conteudo) {
        return chaves.get(nome).cifrar(nome + conteudo);
    }

    private String validarEDescriptografarMensagem(String nome, byte[] conteudo) {
        if (!chaves.containsKey(nome)) {
            throw new IllegalArgumentException("Origem desconhecido");
        }

        Cifrador cifrador = chaves.get(nome);
        String destinoDecifrado = cifrador.decifrar(conteudo);
        if (!destinoDecifrado.startsWith(nome)) {
            throw new IllegalArgumentException("Texto de destino não pode ser decriptografado");
        }

        return destinoDecifrado.substring(nome.length());
    }
}
