package com.procyk.industries.strings;

import com.google.api.services.youtube.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class YoutubeLinkBuilder {
    private static final Logger logger = Logger.getLogger(YoutubeLinkBuilder.class.getName());

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
                videoIds.add(
                        makeYoutubeLinkFromVideoId(searchResult.getId().getVideoId())
                );
            }
        }
        return videoIds;
    }

}
