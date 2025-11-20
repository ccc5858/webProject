package com.example.pojo.result;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String message;
    private Object data;

    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.message = "操作成功";
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.code = 1;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static Result error(String message) {
        Result result = new Result();
        result.code = 0;
        result.message = message;

        return result;
    }
}
