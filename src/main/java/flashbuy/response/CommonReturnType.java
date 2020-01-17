package flashbuy.response;

public class CommonReturnType {
    private String status; // "success" and "fail"

    private Object data; // 这里给Object就很有深意，因为在请求成功时，传进来什么结果就要返回啥结果，相当于包一层

    public static CommonReturnType create(Object data) {
        return CommonReturnType.create(data, "success"); // 函数重载，没给status默认是成功
    }

    public static CommonReturnType create(Object data, String status) {
        CommonReturnType type = new CommonReturnType();

        type.setData(data);
        type.setStatus(status);

        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
