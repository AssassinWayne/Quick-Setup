package me.wh.quicksetup.base;

import me.wh.common.http.data.DataResponse;
import me.wh.quicksetup.R;

/**
 * Created by WayneHu on 16/9/27.
 */
public class ApiError extends RuntimeException {

    private final DataResponse response;

    public ApiError(DataResponse response) {
        this.response = response;
    }

    public DataResponse getResponse() {
        return response;
    }

    public enum ErrorCode {

        UNKNOWN(R.string.error_code_unknown);

        private int errorResId;

        ErrorCode(int errorResId) {
            this.errorResId = errorResId;
        }

        public int getErrorResId() {
            return errorResId;
        }
    }
}
