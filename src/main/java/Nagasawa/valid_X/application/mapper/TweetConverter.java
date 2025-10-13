package Nagasawa.valid_X.application.mapper;

import Nagasawa.valid_X.domain.dto.GetMediaResult;
import Nagasawa.valid_X.domain.dto.GetPostResult;
import Nagasawa.valid_X.domain.dto.MediaCreate;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.dto.TweetView;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TweetConverter {

    // Tweet+Media→PostResultへのマッピング
    @Mappings({
            @Mapping(source = "tweet.tweetId", target = "tweetId"),
            @Mapping(source = "tweet.userId", target = "userId"),
            @Mapping(source = "tweet.content", target = "content"),
            @Mapping(source = "tweet.inReplyToTweetId", target = "inReplyToTweetId"),
            @Mapping(source = "tweet.createdAt", target = "createdAt"),
            @Mapping(source = "medias", target = "medias")
    })
    Nagasawa.valid_X.domain.dto.PostResult toPostResult(Nagasawa.valid_X.domain.model.Tweet tweet, List<Nagasawa.valid_X.domain.model.Media> medias);

    // PostForm→Tweetへのマッピング
    @Mappings({
            @Mapping(target = "tweetId", ignore = true),
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "content", source = "form.content"),
            @Mapping(target = "inReplyToTweetId", source = "form.inReplyToTweet"),
            @Mapping(target = "createdAt", ignore = true)
    })
    Tweet toTweet(Nagasawa.valid_X.domain.dto.PostForm form, Long userId);

    // PostForm→mediaへのマッピング（MediaCreateの項目変更に追従。sha256 は無視）
    @Mappings({
            @Mapping(target = "mediaId", ignore = true),
            @Mapping(target = "mediaType", ignore = true),
            @Mapping(target = "mimeType",  source = "mediaCreate.mimeType"),
            @Mapping(target = "bytes",     ignore = true),
            @Mapping(target = "width",     source = "mediaCreate.width"),
            @Mapping(target = "height",    source = "mediaCreate.height"),
            @Mapping(target = "durationMs",source = "mediaCreate.durationMs"),
            @Mapping(target = "blurhash",  ignore = true),
            @Mapping(target = "storageKey",ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    Media toMedia(Nagasawa.valid_X.domain.dto.MediaCreate mediaCreate);

    // tweetMediaの作成、tweetIdとmediaIdはINSERT後に生成されるので、INSERT後に使用すること
    default List<Nagasawa.valid_X.domain.model.TweetMedia> linkTweetMedias(Long tweetId, List<Long> mediaIdsOrder) {
        List<TweetMedia> links = new ArrayList<>(mediaIdsOrder.size());
        for (int i = 0; i < mediaIdsOrder.size(); i++) {
            TweetMedia tm = TweetMedia.builder()
                    .tweetId(tweetId)
                    .mediaId(mediaIdsOrder.get(i))
                    .position(i)
                    .build();
            links.add(tm);
        }

        return links;
    }

    // TweetView -> GetPostResult（media は後段で詰めるのでここでは無視）
    @Mappings({
            @Mapping(source = "tweetId",           target = "tweetId"),
            @Mapping(source = "userId",            target = "userId"),
            @Mapping(source = "username",          target = "username"),
            @Mapping(source = "content",           target = "content"),
            @Mapping(source = "inReplyToTweetId",  target = "inReplyToTweetId"),
            @Mapping(source = "createdAt",         target = "createdAt"),
            @Mapping(source = "likeCount",         target = "likeCount"),
            @Mapping(source = "repostCount",       target = "repostCount"),
            @Mapping(source = "replyCount",        target = "replyCount"),
            @Mapping(source = "likedByMe",         target = "likedByMe"),
            @Mapping(source = "repostedByMe",      target = "repostedByMe"),
            @Mapping(target = "media", ignore = true)
    })
    GetPostResult toGetPostResult(TweetView row);

    // List<TweetView> -> List<GetPostResult>
    // 単一要素マッピング（toGetPostResult）を使って MapStruct が自動生成します
    List<GetPostResult> toGetPostResults(List<TweetView> rows);

    // media を tweetId ごとにまとめる（tweetId -> List<GetMediaResult>）
    default Map<Long, List<GetMediaResult>> groupMediaByTweetId(List<GetMediaResult> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return Map.of();
        }
        return mediaList.stream()
                .collect(Collectors.groupingBy(GetMediaResult::getTweetId, Collectors.toList()));
    }

    // posts に media を詰めて返す（不変DTOのため toBuilder で部分更新）
    default List<GetPostResult> mergePostsWithMedia(List<GetPostResult> posts, List<GetMediaResult> mediaList) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        Map<Long, List<GetMediaResult>> mediaMap =
                groupMediaByTweetId(mediaList == null ? List.of() : mediaList);

        List<GetPostResult> enriched = new ArrayList<>(posts.size());
        for (GetPostResult p : posts) {
            List<GetMediaResult> medias = mediaMap.getOrDefault(p.getTweetId(), List.of());
            enriched.add(p.toBuilder().media(medias).build());
        }
        return enriched;
    }






}
