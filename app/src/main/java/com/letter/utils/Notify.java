package com.letter.utils;

import java.util.List;

public class Notify {

    private List<NotifyBean> notify;

    public List<NotifyBean> getNotify() {
        return notify;
    }

    public void setNotify(List<NotifyBean> notify) {
        this.notify = notify;
    }

    public static class NotifyBean {
        /**
         * time : -1
         * title : default
         * content : ["斯人若彩虹，遇上方知有"]
         */

        private long time;
        private String title;
        private List<String> content;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getContent() {
            return content;
        }

        public void setContent(List<String> content) {
            this.content = content;
        }
    }
}
