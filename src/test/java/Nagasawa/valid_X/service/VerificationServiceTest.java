package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.VerificationService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class VerificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private SecureRandom secureRandom;
    @InjectMocks
    private VerificationService verificationService;

    @Test
    @DisplayName("正常系: UrlトークンがBase64形式で大体16バイト相当の長さを持つ")
    void generateVerificationUrlToken_urlTokenHasApproximately16Bytes() {

        // fakeBytesをSecureRandomが呼ばれた際に渡す
        byte[] fakeBytes = new byte[16];
        for (int i = 0; i < 16; i++) fakeBytes[i] = (byte) i;
        doAnswer(inv -> {
            System.arraycopy(fakeBytes, 0, inv.getArgument(0), 0, 16);
            return null;
        }).when(secureRandom).nextBytes(any());

        // 実行
        String urlToken = verificationService.generateVerificationUrlToken();

        // 基本的なURLセーフの検証
        assertThat(urlToken).doesNotContain("+", "/", "=");
        // Base64形式では3バイトを4文字に変換する
        assertThat(urlToken.length()).isBetween(20, 24);
    }

    @Test
    @DisplayName("同じトークンは同じハッシュを返す、異なるトークンは異なるハッシュ")
    void hashToken_producesConsistentHash() {
        byte[] h1 = verificationService.hashToken("abc");
        byte[] h2 = verificationService.hashToken("abc");
        byte[] h3 = verificationService.hashToken("xyz");

        assertThat(h1).isEqualTo(h2);
        assertThat(h1).isNotEqualTo(h3);
        // SHA-256は32バイト固定
        assertThat(h1).hasSize(32);
    }

    @Test
    @DisplayName("正常系: メール送信時にMimeMessageが正しく構成され、JavaMailSenderが呼ばれる")
    void sendVerificationMail_sendsExpectedMail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        verificationService.sendVerificationMail("user@example.com", "token123");

        // JavaMailSender#send が呼ばれたことを確認
        verify(javaMailSender).send(mimeMessage);

        // 内容の検証
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(mimeMessage.getSubject()).contains("Verify your account");
        assertThat(mimeMessage.getContent().toString())
                .contains("https://example.com/verify?token=token123");
    }
}
