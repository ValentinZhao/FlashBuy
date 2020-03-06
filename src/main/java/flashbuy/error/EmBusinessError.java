package flashbuy.error;

public enum EmBusinessError implements CommonError {
    // 10000开头代表通用错误
    INVALID_PARAMETER(10001, "入参校验失败"),

    UNKNOWN_PARAMETER(10002, "未知错误"),

    // 20000开头代表用户错误
    USER_NOT_EXIST(20001, "用户不存在"),

    LOGIN_FAILURE(20002, "登录手机或密码错误"),

    USER_NOT_LOGIN(20003, "用户还未登陆"),

    // 30000开头表示订单错误
    STOCK_NOT_ENOUGH(30001, "库存不足"),
    MQ_ASYNC_EXCEPTION(30002, "消息队列异步任务失败"),
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
