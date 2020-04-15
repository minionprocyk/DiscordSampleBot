package com.procyk.industries.strings;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YoutubeLinkBuilderTest{
    @Test
    public void testYoutubeLinkBuilder() {
        assertThrows(NullPointerException.class, ()->YoutubeLinkBuilder.makeYoutubeLinkFromVideoId(null));
        assertTrue(YoutubeLinkBuilder.makeYoutubeLinkFromVideoId("v123").contains("v123"));
        assertTrue(YoutubeLinkBuilder.makeYoutubeLinkFromVideoId("1642hike").contains("youtube"));
    }

    @Test
    public void testYoutubeLinkListBuilder() {
        String videoId="test123";
        SearchResult result = mock(SearchResult.class);
        ResourceId resourceId = mock(ResourceId.class);
        when(result.getId()).thenReturn(resourceId);
        when(resourceId.getVideoId()).thenReturn(videoId);

        List<SearchResult> resultList = Arrays.asList(result);
        for(String s : YoutubeLinkBuilder.makeYoutubeLinksFromSearchResult(resultList)) {
            assertTrue(s.contains(videoId));
        }
    }
}
