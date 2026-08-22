package com.soda.risk.engine.api.dto;

import com.soda.risk.engine.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;
    private String serverIp;
    private long timestamp;

    public static <T> Response<T> success() {
        return Response.<T>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMsg())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMsg())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Response<T> fail(CodeEnum codeEnum) {
        return Response.<T>builder()
                .code(codeEnum.getCode())
                .msg(codeEnum.getMsg())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Response<T> fail(int code, String msg) {
        return Response.<T>builder()
                .code(code)
                .msg(msg)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public Response(CodeEnum codeEnum) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isSuccess() {
        return this.code == CodeEnum.SUCCESS.getCode();
    }
}
