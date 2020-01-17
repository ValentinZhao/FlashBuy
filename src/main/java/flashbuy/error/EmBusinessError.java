package flashbuy.error;

public enum EmBusinessError implements CommonError {
    // 00000开头代表通用错误
    INVALID_PARAMETER(00001, "入参校验失败"),

    // 10000开头代表用户错误
    USER_NOT_EXIST(10001, "用户不存在")
    ;

    private int errCode;

    private String errMsg;

    EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}