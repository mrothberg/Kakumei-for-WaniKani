package com.mrothberg.kakumei.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class SessionNavigation {
    private SessionNavigation() {}

    /** Session pages must confirm exit rather than navigate into their redirect history. */
    public static boolean isSessionUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (!"www.wanikani.com".equalsIgnoreCase(host)
                    && !"wanikani.com".equalsIgnoreCase(host)) return false;
            String path = uri.getPath();
            return isPathWithin(path, "/lesson") || isPathWithin(path, "/review")
                    || isPathWithin(path, "/subject-lessons")
                    || isPathWithin(path, "/subjects/review");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean isPathWithin(String path, String root) {
        return path != null && (path.equals(root) || path.startsWith(root + "/"));
    }
}
