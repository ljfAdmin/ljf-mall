package com.ljf.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int resultCode;
    private String message;
    private T data;

    public Result(){}

    public Result(int resultCode,String message){
        this.resultCode = resultCode;
        this.message = message;
    }
}
