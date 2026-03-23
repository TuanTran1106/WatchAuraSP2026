package com.example.watchaura.util;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class PaginationWindow {

    private PaginationWindow() {}

    public static class PageItem {
        private final boolean ellipsis;
        private final int pageIndex;

        public PageItem(boolean ellipsis, int pageIndex) {
            this.ellipsis = ellipsis;
            this.pageIndex = pageIndex;
        }

        public boolean isEllipsis() { return ellipsis; }
        public int getPageIndex()   { return pageIndex; }
    }

    public static <T> List<PageItem> build(Page<T> page, int neighborCount) {
        int total = page.getTotalPages();
        if (total <= 0) return List.of();

        int current = page.getNumber();
        TreeSet<Integer> indices = new TreeSet<>();
        indices.add(0);
        indices.add(total - 1);
        for (int i = current - neighborCount; i <= current + neighborCount; i++) {
            if (i >= 0 && i < total) indices.add(i);
        }

        List<PageItem> result = new ArrayList<>();
        Integer prev = null;
        for (int p : indices) {
            if (prev != null && p - prev > 1) result.add(new PageItem(true, -1));
            result.add(new PageItem(false, p));
            prev = p;
        }
        return result;
    }
}
