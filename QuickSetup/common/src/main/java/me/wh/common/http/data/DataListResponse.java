package me.wh.common.http.data;

import java.util.List;

public class DataListResponse<T> extends DataResponse {
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
