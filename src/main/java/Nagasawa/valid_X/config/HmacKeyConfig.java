// HmacKeyConfig.java
package Nagasawa.valid_X.config;

import Nagasawa.valid_X.domain.dto.HmacKeyProvider;
import Nagasawa.valid_X.domain.dto.InMemoryHmacKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HmacKeyConfig {

    @Value("${app.hmac.active:1}")
    private short active;

    // 64桁HEX(=32バイト)の例。必要に応じて複数世代
    @Value("${app.hmac.k1:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef}")
    private String k1;

    @Value("${app.hmac.k2:}")
    private String k2;

    @Bean
    public HmacKeyProvider hmacKeyProvider() {
        Map<Short, byte[]> keysRaw = new HashMap<>();
        if (!k1.isBlank()) keysRaw.put((short)1, hexDecode(k1));
        if (!k2.isBlank()) keysRaw.put((short)2, hexDecode(k2));
        return new InMemoryHmacKeyProvider(keysRaw, active);
    }

    private static byte[] hexDecode(String s){
        int len = s.length();
        if ((len & 1) != 0) throw new IllegalArgumentException("hex length must be even");
        byte[] out = new byte[len/2];
        for (int i=0; i<len; i+=2) {
            out[i/2] = (byte) Integer.parseInt(s.substring(i, i+2), 16);
        }
        return out;
    }
}