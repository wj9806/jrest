package io.github.wj9806.jrest.test;


public class Result<T> {

    private String code;

    private String message;

    private T data;

    public static <T> Result<T> success(T data) {
    	Result<T> result = new Result<>();
    	result.setCode("200");
    	result.setMessage("成功");
    	result.setData(data);
    	return result;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
