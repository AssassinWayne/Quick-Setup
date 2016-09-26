package me.wh.common.http.data;

public class DataSingleResponse<T> extends DataResponse {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
