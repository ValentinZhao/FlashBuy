package flashbuy.controller;

import flashbuy.controller.viewobject.UserVO;
import flashbuy.dao.UserInfoMapper;
import flashbuy.dataobject.UserInfo;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.response.CommonReturnType;
import flashbuy.service.UserService;
import flashbuy.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static flashbuy.util.Utils.convertModel2VO;

@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserService service;

    @Autowired
    HttpServletRequest servletRequest;

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


    @RequestMapping(value = "/getopt",method = {RequestMethod.POST},consumes={"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType getOTP(@RequestParam(name="tel") String phoneNumber) {
        // 生成随机码
        Random random = new Random();
        int optCode = random.nextInt(899999) + 100000;

        // 同手机号绑定session
        servletRequest.getSession().setAttribute(phoneNumber, optCode);

        // 模拟短信推送
        System.out.println("手机尾号" + phoneNumber.substring(7) + "的OPT短信验证码为：" + optCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes={"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="tel") String phoneNumber,
                                     @RequestParam(name="otp") String otpCode,
                                     @RequestParam(name="name") String name,
                                     @RequestParam(name="gender") Integer gender,
                                     @RequestParam(name="age") Integer age,
                                     @RequestParam(name="psw") String psw) throws BusinessException, NoSuchAlgorithmException {
        //验证手机号和对应的otpCode相符合
        String oriOtpCode = String.valueOf(servletRequest.getSession().getAttribute(phoneNumber));
        if (!otpCode.equals(oriOtpCode))
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER, "短信验证码不一致");

        UserModel model = new UserModel();
        model.setName(name);
        model.setPhone(phoneNumber);
        model.setAge(age);
        model.setGender(gender == 0);
        model.setEncrptPassword(this.encodeByMd5(psw));
        model.setRegisterMode("byphone");
        service.register(model);

        return CommonReturnType.create(null);
    }

    @PostMapping(path = "/login", consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="phone") String phone,
                                  @RequestParam(name="psw") String psw) throws BusinessException, NoSuchAlgorithmException {
        UserModel model = service.validateLogin(phone, this.encodeByMd5(psw));

        servletRequest.getSession().setAttribute("IS_LOGIN", true);
        servletRequest.getSession().setAttribute("LOGIN_USER", model);

        return CommonReturnType.create(null);
    }

    private String encodeByMd5(String psw) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        BASE64Encoder encoder = new BASE64Encoder();

        String encryptedPsw = encoder.encode(digest.digest(psw.getBytes(StandardCharsets.UTF_8)));
        return encryptedPsw;
    }

}
