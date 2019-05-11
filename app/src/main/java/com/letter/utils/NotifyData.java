package com.letter.utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotifyData {
    @SerializedName("notify")
    private List<Data> notifyList;

    public List<Data> getNotifyList() {
        return notifyList;
    }

    public void setNotifyList(List<Data> notifyList) {
        this.notifyList = notifyList;
    }

    public class Data{
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

}
