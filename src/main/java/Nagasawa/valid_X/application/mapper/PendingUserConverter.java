package Nagasawa.valid_X.application.mapper;

import Nagasawa.valid_X.domain.model.UserPassword;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PendingUserConverter {

    // PendingUser → Userへのマッピング
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "displayName", target = "displayName"),
            @Mapping(source = "locale", target = "locale"),
            @Mapping(source = "timezone", target = "timezone"),
            @Mapping(source = "createdAt", target = "createdAt")
    })
    Nagasawa.valid_X.domain.model.User toUser(Nagasawa.valid_X.domain.model.PendingUser pendingUser);

    // PendingUser → UserEmailへのマッピング
    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(source = "email", target = "email"),
            @Mapping(source = "createdAt", target = "createdAt")
    })
    Nagasawa.valid_X.domain.model.UserEmail toUserEmail(Nagasawa.valid_X.domain.model.PendingUser pendingUser);

    // PendingUser → Profileへのマッピング
    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(target = "bio", ignore = true),
            @Mapping(target = "avatarUrl", ignore = true),
            @Mapping(target = "protected_", constant = "false"),
            @Mapping(source = "createdAt", target = "createdAt"),
            @Mapping(source = "updatedAt", target = "updatedAt")
    })
    Nagasawa.valid_X.domain.model.Profile toProfile(Nagasawa.valid_X.domain.model.PendingUser pendingUser);

    // PendingUser → Countへのマッピング
    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(target = "followers", constant = "0"),
            @Mapping(target = "following", constant = "0"),
            @Mapping(target = "tweets", constant = "0"),
            @Mapping(source = "updatedAt", target = "updatedAt")
    })
    Nagasawa.valid_X.domain.model.Count toCount(Nagasawa.valid_X.domain.model.PendingUser pendingUser);

    // PendingUser → UserPasswordsへのマッピング
    @Mappings({
            @Mapping(source = "id", target = "userId"),
            @Mapping(source = "passwordHash", target = "passwordHash"),
            @Mapping(target = "algorithm", constant = "bcrypt"),
            @Mapping(target = "strength", constant = "12"),
            @Mapping(source = "updatedAt", target = "passwordUpdatedAt"),
            @Mapping(target = "rehashRequired", constant = "false")
    })
    UserPassword toUserPasswords(Nagasawa.valid_X.domain.model.PendingUser pendingUser);
}

