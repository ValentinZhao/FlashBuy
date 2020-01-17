package flashbuy.util;

import flashbuy.controller.viewobject.UserVO;
import flashbuy.service.model.UserModel;
import org.springframework.beans.BeanUtils;

public class Utils {
    public static UserVO convertModel2VO(UserModel userModel, UserVO userVO) {
        if (userModel == null) return null;

        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }
}
