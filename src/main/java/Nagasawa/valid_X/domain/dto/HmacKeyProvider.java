package Nagasawa.valid_X.domain.dto;

import javax.crypto.SecretKey;

public interface HmacKeyProvider {
    short activeKeyId();
    SecretKey keyOf(short kid);
}
