package com.ljf.utils;

import org.apache.commons.lang3.StringUtils;

public class ResultGenerator {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_FAIL_MESSAGE = "FAIL";
    private static final int RESULT_CODE_SUCCESS = 200;
    private static final int RESULT_CODE_SERVER_ERROR = 500;

    private ResultGenerator(){}

    public static Result genSuccessResult(){
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(DEFAULT_SUCCESS_MESSAGE);
        return result;
    }
    public static Result genSuccessResult(String message){
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(message);
        return result;
    }
    public static Result genSuccessResult(Object data) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(DEFAULT_SUCCESS_MESSAGE);
        result.setData(data);
        return result;
    }
    public static Result genSuccessResult(Object data,String message) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static Result genFailResult(String message) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SERVER_ERROR);
        if(StringUtils.isEmpty(message)){
            result.setMessage(DEFAULT_FAIL_MESSAGE);
        }else{
            result.setMessage(message);
        }
        return result;
    }

    public static Result genErrorResult(int code,String message){
        Result result = new Result();
        result.setResultCode(code);
        if(StringUtils.isEmpty(message)){
            result.setMessage(DEFAULT_FAIL_MESSAGE);
        }else{
            result.setMessage(message);
        }
        return result;
    }

    //public static Result genDmlResult(boolean flag){
    //    return flag ? genSuccessResult() : genFailResult(ToFrontMessageConstantEnum.DB_ERROR.getResult());
    //}

}
