import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class Usuario {
    private final Logger LOG = Logger.getLogger(Usuario.class.getName());

    private final String nome;
    private Cifrador cifrador;
    private KDC kdc;
    private Map<String, Sessao> sessoes;

    public Usuario(KDC kdc, String nome, String chave) {
        this.nome = nome;
        this.cifrador = kdc.criarCifrador(chave);
        this.kdc = kdc;

        sessoes = new HashMap<>();
    }

    public void enviarMensagem(Usuario destino, String mensagem) {
        Sessao sessao = pegaOuCriaSessao(destino);

        byte[] mensagemCifrada = sessao.cifrar(nome + mensagem);

        LOG.info(() -> MessageFormat.format("{0}: enviando mensagem cifrada para {1}: {2}", nome, destino.nome, new String(Base64.getEncoder().encode(mensagemCifrada))));
//        System.out.printf("%s: enviando mensagem cifrada para %s: %s%n", nome, destino.nome, Arrays.toString(mensagemCifrada));

        destino.receberMensagem(this, mensagemCifrada);
    }

    public void receberMensagem(Usuario origem, byte[] mensagemCifrada) {
        if (!sessoes.containsKey(origem.nome)) {
            byte[] chaveSessaoCifrada = kdc.recuperarSessao(nome, cifrador.cifrar(nome + origem.nome));
            String chaveSessaoDecifrada = removeNomeConcatenadoDoConteudo(cifrador.decifrar(chaveSessaoCifrada));

            long nonceOriginal = new Random().nextLong();

            Sessao sessao = kdc.criarSessao(chaveSessaoDecifrada);

            byte[] nonceCifrado = sessao.cifrar(Long.toString(nonceOriginal));

            long nonceDecifrado = origem.decifrarNonce(this, nonceCifrado);

            if (nonceOriginal != nonceDecifrado) {
                throw new RuntimeException("mensagem não pode ser recebida");
            }

            sessoes.put(origem.nome, sessao);
        }

        System.out.printf("%s: mensagem recebida de %s: %s%n", nome, origem.nome, sessoes.get(origem.nome).decifrar(mensagemCifrada).substring(origem.nome.length()));
    }

    private Sessao pegaOuCriaSessao(Usuario destino) {
        if (sessoes.containsKey(destino.nome))
            return sessoes.get(destino.nome);

        byte[] chaveSessaoCifrada = kdc.iniciarSessao(nome, this.cifrador.cifrar(nome + destino.nome));

        Sessao sessao = kdc.criarSessao(removeNomeConcatenadoDoConteudo(cifrador.decifrar(chaveSessaoCifrada)));

        sessoes.put(destino.nome, sessao);

        return sessao;
    }

    private String removeNomeConcatenadoDoConteudo(String conteudo) {
        if (!conteudo.substring(0, nome.length()).equals(nome))
            throw new RuntimeException("erro ao descriptografar");
        return conteudo.substring(nome.length());
    }

    private long decifrarNonce(Usuario usuario, byte[] nonceCifrado) {
        return Long.parseLong(pegaOuCriaSessao(usuario).decifrar(nonceCifrado));
    }

}
