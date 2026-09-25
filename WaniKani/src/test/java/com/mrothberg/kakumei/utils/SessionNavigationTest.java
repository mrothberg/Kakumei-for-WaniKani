package com.mrothberg.kakumei.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionNavigationTest {
    @Test
    public void recognizesLegacySessionAndSummaryPages() {
        assertTrue(SessionNavigation.isSessionUrl("https://www.wanikani.com/lesson/session"));
        assertTrue(SessionNavigation.isSessionUrl("https://www.wanikani.com/review/session"));
        assertTrue(SessionNavigation.isSessionUrl("http://www.wanikani.com/review/summary"));
    }

    @Test
    public void recognizesRedirectedSessions() {
        assertTrue(SessionNavigation.isSessionUrl("https://www.wanikani.com/subject-lessons/123/456"));
        assertTrue(SessionNavigation.isSessionUrl("https://wanikani.com/subjects/review?queue=1"));
    }

    @Test
    public void ordinaryPagesCanUseWebHistory() {
        assertFalse(SessionNavigation.isSessionUrl("https://www.wanikani.com/vocabulary/example"));
        assertFalse(SessionNavigation.isSessionUrl("https://www.wanikani.com/lesson-notes"));
        assertFalse(SessionNavigation.isSessionUrl("https://example.com/lesson/session"));
        assertFalse(SessionNavigation.isSessionUrl("https://www.wanikani.com.evil.example/review"));
    }

    @Test
    public void toleratesMissingOrMalformedUrls() {
        assertFalse(SessionNavigation.isSessionUrl(null));
        assertFalse(SessionNavigation.isSessionUrl(""));
        assertFalse(SessionNavigation.isSessionUrl("not a URL"));
    }
}
