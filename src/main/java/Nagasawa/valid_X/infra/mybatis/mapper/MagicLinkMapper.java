package Nagasawa.valid_X.infra.mybatis.mapper;

import Nagasawa.valid_X.domain.model.MagicLink;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MagicLinkMapper {
    int insert(MagicLink magicLink);

    Long consumeAndReturnUserId(byte[] tokenHash, short kid);
}
