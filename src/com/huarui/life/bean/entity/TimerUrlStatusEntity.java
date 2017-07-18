package com.huarui.life.bean.entity;

/**
 * Created by HR_Life on 2016/11/10 14:42 15:19.
 */

public class TimerUrlStatusEntity {

    /**
     * status : 0
     * data : {"url":"http://www.baidu.com","version":3,"txt":"http://192.168.1.153/laravel5.1/public/app/file.txt"}
     */

    private String status;
    private DataBean data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * url : http://www.baidu.com
         * version : 3
         * txt : http://192.168.1.153/laravel5.1/public/app/file.txt
         */

        private String url;
        private int version;
        private String txt;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }
    }
}
