package Nagasawa.valid_X.service;

import Nagasawa.valid_X.application.service.SearchUserService;
import Nagasawa.valid_X.domain.dto.Page;
import Nagasawa.valid_X.domain.dto.SearchUserSummary;
import Nagasawa.valid_X.infra.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchUserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SearchUserService searchUserService;

    private List<SearchUserSummary> mockResults;

    @BeforeEach
    void setUp() {
        // モック検索結果の生成
        SearchUserSummary user1 = SearchUserSummary.builder()
                .id(1L)
                .username("user_test1")
                .displayName("UserDispla1")
                .build();
        SearchUserSummary user2 = SearchUserSummary.builder()
                .id(1L)
                .username("user_test2")
                .displayName("UserDisplay2")
                .build();
        SearchUserSummary user3 = SearchUserSummary.builder()
                .id(1L)
                .username("user_test3")
                .displayName("UserDisplay3")
                .build();

        mockResults = List.of(user1, user2, user3);
    }

    @Test
    @DisplayName("正常系: 正しくユーザー情報が返却され、カーソルは設定されない")
    void searchUser_withinLimit() {
        // limit = 5 (mockResults.size() = 3 → hasNext=false)
        when(userMapper.searchByDisplayNameOrUsernamePrefix("u", null, 5))
                .thenReturn(mockResults);

        Page<SearchUserSummary> page = searchUserService.searchUser("u", null, 5);

        assertThat(page.getItems().size()).isEqualTo(3);
        assertThat(page.getNextCursor()).isNull();
        verify(userMapper, times(1))
                .searchByDisplayNameOrUsernamePrefix("u", null, 5);
    }

    @Test
    @DisplayName("正常系: 正しくユーザー情報が返却され、カーソルは設定される")
    void searchUser_exceedsLimit() {
        // limit = 2 (mockResults.size() = 3 → hasNext=true)
        when(userMapper.searchByDisplayNameOrUsernamePrefix("u", null, 2))
                .thenReturn(mockResults);

        Page<SearchUserSummary> page = searchUserService.searchUser("u", null, 2);

        assertThat(page.getItems().size()).isEqualTo(2); // limit件まで
        assertThat(page.getNextCursor()).isEqualTo(1L); // 最後に返されたユーザーのID
    }

    @Test
    @DisplayName("正常系: ユーザー情報がヒットしない")
    void searchUser_userEmptyResult() {
        when(userMapper.searchByDisplayNameOrUsernamePrefix("z", null, 3))
                .thenReturn(List.of());

        Page<SearchUserSummary> page = searchUserService.searchUser("z", null, 3);

        assertThat(page.getItems().size()).isEqualTo(0);
        assertThat(page.getNextCursor()).isNull();
    }
}