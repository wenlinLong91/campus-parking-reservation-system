package com.segi.parking.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;    // 状态码：200 成功，500 失败
    private String message;  // 提示信息
    private T data;          // 数据承载主体

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("Success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}