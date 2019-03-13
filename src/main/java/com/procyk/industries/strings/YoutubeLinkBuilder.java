package com.procyk.industries.strings;

import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class YoutubeLinkBuilder {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeLinkBuilder.class);

    private YoutubeLinkBuilder() {}
    /**
     * Constructs a youtube video link from a youtube videoId
     * @param videoId
     */
    public static String makeYoutubeLinkFromVideoId(String videoId) {
        return "https://www.youtube.com/watch?v=".concat(videoId);
    }
    public static List<String> makeYoutubeLinksFromSearchResult(List<SearchResult> searchResults) {
        List<String> videoIds = new ArrayList<>();
        if(searchResults!=null) {
            for(SearchResult searchResult : searchResults) {
                logger.info("Making youtube link from search result {}", searchResult);
                videoIds.add(
                        makeYoutubeLinkFromVideoId(searchResult.getId().getVideoId())
                );
            }
        }
        return videoIds;
    }

}
