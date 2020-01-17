package flashbuy.error;

public class BusinessException extends Exception implements CommonError {

    // 这个其实就是传进来的枚举类型了
    private CommonError commonError;

    public BusinessException (CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    // 自定义错误信息拓展
    public BusinessException (CommonError commonError, String msg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(msg);
    }

    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        return this.commonError.setErrMsg(errMsg);
    }
}
