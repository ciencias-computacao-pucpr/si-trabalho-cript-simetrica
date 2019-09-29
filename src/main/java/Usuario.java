import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Usuario {
    private final String nome;
    private Cifrador cifrador;

    private Map<String, Sessao> sessoes;

    public Usuario(String nome, String chave) {
        this.nome = nome;
        this.cifrador = new Cifrador(chave);

        sessoes = new HashMap<>();
        KDC.getInstancia().adicionarChave(nome, chave);
    }

    public void enviarMensagem(Usuario destino, String mensagem) {
        Sessao sessao = pegaOuCriaSessao(destino);

        destino.receberMensagem(this, sessao.cifrar(nome+mensagem));
    }

    private Sessao pegaOuCriaSessao(Usuario destino) {
        if (sessoes.containsKey(destino.nome))
            return sessoes.get(destino.nome);

        byte[] chaveSessaoCifrada = KDC.getInstancia().iniciarSessao(nome, this.cifrador.cifrar(nome + destino.nome));

        Sessao sessao = new Sessao(removeNomeConcatenadoDoConteudo(cifrador.decifrar(chaveSessaoCifrada)));

        sessoes.put(destino.nome, sessao);

        return sessao;
    }

    private String removeNomeConcatenadoDoConteudo(String conteudo) {
        if (!conteudo.substring(0, nome.length()).equals(nome))
            throw new RuntimeException("erro ao descriptografar");
        return conteudo.substring(nome.length());
    }

    public void receberMensagem(Usuario origem, byte[] mensagemCifrada) {
        if (!sessoes.containsKey(origem.nome)) {
            byte[] chaveSessaoCifrada = KDC.getInstancia().getSessaoCom(nome, cifrador.cifrar(nome + origem.nome));
            String chaveSessaoDecifrada = removeNomeConcatenadoDoConteudo(cifrador.decifrar(chaveSessaoCifrada));

            long nonceOriginal = new Random().nextLong();

            Sessao sessao = new Sessao(chaveSessaoDecifrada);

            byte[] nonceCifrado = sessao.cifrar(Long.toString(nonceOriginal));

            long nonceDecifrado = origem.decifrarNonce(this, nonceCifrado);

            if (nonceOriginal != nonceDecifrado) {
                throw new RuntimeException("mensagem não pode ser recebida");
            }

            sessoes.put(origem.nome, sessao);
        }

        System.out.printf("Mensagem recebida de %s: %s%n", origem.nome, sessoes.get(origem.nome).decifrar(mensagemCifrada).substring(origem.nome.length()));
    }

    private long decifrarNonce(Usuario usuario, byte[] nonceCifrado) {
        return Long.parseLong(pegaOuCriaSessao(usuario).decifrar(nonceCifrado));
    }

}
