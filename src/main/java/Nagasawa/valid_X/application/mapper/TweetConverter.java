package Nagasawa.valid_X.application.mapper;

import Nagasawa.valid_X.domain.dto.MediaCreate;
import Nagasawa.valid_X.domain.dto.PostForm;
import Nagasawa.valid_X.domain.model.Media;
import Nagasawa.valid_X.domain.model.Tweet;
import Nagasawa.valid_X.domain.model.TweetMedia;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;

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

    // PostForm→mediaへのマッピング
    @Mappings({
            @Mapping(target = "mediaId", ignore = true),
            @Mapping(target = "mediaType", source = "mediaCreate.mediaType"),
            @Mapping(target = "mimeType", source = "mediaCreate.mimeType"),
            @Mapping(target = "bytes", source = "mediaCreate.bytes"),
            @Mapping(target = "width", source = "mediaCreate.width"),
            @Mapping(target = "height", source = "mediaCreate.height"),
            @Mapping(target = "durationMs", source = "mediaCreate.durationMs"),
            @Mapping(target = "sha256", source = "mediaCreate.sha256"),
            @Mapping(target = "blurhash", source = "mediaCreate.blurhash"),
            @Mapping(target = "storageKey", source = "mediaCreate.storageKey"),
            @Mapping(target = "createdAt", ignore = true)
    })
    Media toMedia(Nagasawa.valid_X.domain.dto.MediaCreate mediaCreate);

    // mediasの作成
    @IterableMapping(qualifiedByName = "mapMedia")
    default List<Nagasawa.valid_X.domain.model.Media> toMedias(Nagasawa.valid_X.domain.dto.PostForm form) {
        if (form.medias() == null) return List.of();
        List<Media> list = new ArrayList<>(form.medias().size());
        for (MediaCreate m : form.medias()) {
            list.add(toMedia(m));
        }
        return list;
    }

    // tweetMediaの作成、tweetIdとmediaIdはINSERT後に生成されるので、INSERT後に使用すること
    default List<Nagasawa.valid_X.domain.model.TweetMedia> linkTweetMedias(Long tweetId, List<Long> mediaIdsOrder) {
        List<TweetMedia> links = new ArrayList<>(mediaIdsOrder.size());
        for (int i = 0; i < mediaIdsOrder.size(); i++) {
            TweetMedia tm = new TweetMedia();
            tm.setTweetId(tweetId);
            tm.setMediaId(mediaIdsOrder.get(i));
            tm.setPosition(i);
            links.add(tm);
        }

        return links;
    }






}
