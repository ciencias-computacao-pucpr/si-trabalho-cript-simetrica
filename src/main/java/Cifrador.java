import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Cifrador {

    private String algoritimo;

    public byte[] cifrar(String mensagem) {
        try {
            Cipher cipher = criarCipher(Cipher.ENCRYPT_MODE);
            return cipher.doFinal(mensagem.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decifrar(byte[] mensagem) {
        try {
            Cipher cipher = criarCipher(Cipher.DECRYPT_MODE);
            return new String(cipher.doFinal(mensagem));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Key chave;

    public Cifrador(String chave, String algoritimo) {
        this.chave = new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), algoritimo);
        this.algoritimo = algoritimo;
    }

    private Cipher criarCipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance(algoritimo);
            cipher.init(mode, this.chave);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
