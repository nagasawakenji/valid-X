package Nagasawa.valid_X.application.service;


import Nagasawa.valid_X.domain.dto.Page;
import Nagasawa.valid_X.domain.dto.SearchUserSummary;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchUserService {

    private final UserMapper userMapper;

    private static <T extends SearchUserSummary> Page<T> toPage(List<T> rows, int limit) {
        boolean hasNext = rows.size() > limit;
        List<T> pageItems = hasNext ? rows.subList(0, limit) : rows;

        // 次カーソルは「返した最後のツイートのID」
        Long nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            T lastReturned = pageItems.get(pageItems.size() - 1);
            nextCursor = lastReturned.getId();
        }

        // 次のpost取得でnextCursor未満のものが取得される
        return new Page<>(pageItems, nextCursor);
    }

    @Transactional
    public Page<SearchUserSummary> searchUser(String prefix, Long cursor, int limit) {

        List<SearchUserSummary> searchUserSummaries = userMapper.searchByDisplayNameOrUsernamePrefix(
                prefix,
                cursor,
                limit
        );

        return toPage(searchUserSummaries, limit);
    }
}
