package flashbuy.controller;

import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BaseController {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest request, Exception ex) {
        Map<String, Object> responseMap = new HashMap<>();

        if (ex instanceof BusinessException) {
            responseMap.put("errCode", ((BusinessException) ex).getErrCode());
            responseMap.put("errMsg", ((BusinessException) ex).getErrMsg());
        } else {
            responseMap.put("errCode", EmBusinessError.UNKNOWN_PARAMETER.getErrCode());
            responseMap.put("errMsg", EmBusinessError.UNKNOWN_PARAMETER.getErrMsg());
        }

        return CommonReturnType.create(responseMap, "fail");
    }
}
