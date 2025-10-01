package Nagasawa.valid_X.domain.dto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryHmacKeyProvider implements HmacKeyProvider{
    private final Map<Short, SecretKey> keys;
    private final short active;

    public InMemoryHmacKeyProvider(Map<Short, byte[]> keysRaw, short active) {
        this.keys = keysRaw.entrySet().stream().collect(
            Collectors.toMap(
                    e -> e.getKey(),
                    e -> new SecretKeySpec(e.getValue(), "HmacSHA256")
                )
            );
        this.active = active;
    }

    @Override
    public short activeKeyId() { return active; }
    @Override
    public SecretKey keyOf(short kid) { return keys.get(kid); }
}
