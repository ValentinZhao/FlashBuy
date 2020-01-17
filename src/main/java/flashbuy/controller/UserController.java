package flashbuy.controller;

import flashbuy.controller.viewobject.UserVO;
import flashbuy.dao.UserInfoMapper;
import flashbuy.dataobject.UserInfo;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.response.CommonReturnType;
import flashbuy.service.UserService;
import flashbuy.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static flashbuy.util.Utils.convertModel2VO;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController{

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserService service;

    @GetMapping("/")
    public String home() {
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(1);

        if (userInfo == null) return "当前无用户";

        return userInfo.getName();
    }

    @GetMapping("/get")
    public CommonReturnType getUserById(@RequestParam(name="user_id") int id) throws BusinessException {
        UserModel userModel = service.getUserById(id);
        UserVO userVO = new UserVO();

        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        // 将核心领域模型对象转化成可供模型是用的view object
        userVO = convertModel2VO(userModel, userVO);

        // 通用处理层包装一下
        return CommonReturnType.create(userVO);
    }

}
