package flashbuy.service.impl;

import com.alibaba.druid.util.StringUtils;
import flashbuy.dao.UserInfoMapper;
import flashbuy.dao.UserPasswordMapper;
import flashbuy.dataobject.UserInfo;
import flashbuy.dataobject.UserPassword;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.service.UserService;
import flashbuy.service.model.UserModel;
import flashbuy.validator.ValidationResult;
import flashbuy.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserPasswordMapper userPasswordMapper;

    @Autowired
    private ValidatorImpl validator;

    /**
     * 这里面的话，需要注意由于我们要从密码表里面取密码，结合起来信息set给UserModel才可以进行
     * 后面的查询之类的
     */
    @Override
    public UserModel getUserById(int id) {
        UserInfo info = userInfoMapper.selectByPrimaryKey(id);

        if (info == null) return null;

        // 我们需要改造user password mapper里面的方法，让它支持从user id来取对应的密码
        UserPassword psw = userPasswordMapper.selectByUserId(info.getId());

        if (psw == null) return null;


        return aggregate(info, psw);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null)
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER);

        ValidationResult result =  validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER, result.getErrMsg());
        }
        // 省下下面这一堆东西，注意UserModel里面的对应注解，把错误信息直接给到result里面了
//        if (userModel.getGender() == null ||
//            userModel.getAge() == null ||
//            StringUtils.isEmpty(userModel.getPhone()) ||
//            StringUtils.isEmpty(userModel.getName()) ||
//            StringUtils.isEmpty(userModel.getEncrptPassword())
//        )

        UserInfo info = convertUM2Info(userModel);
        userInfoMapper.insertSelective(info);

        userModel.setId(info.getId()); // 存过之后才有自增的id

        UserPassword psw = convertUM2Psw(userModel);

        userPasswordMapper.insertSelective(psw);
    }

    @Override
    public UserModel validateLogin(String phone, String psw) throws BusinessException {
        UserInfo info = userInfoMapper.selectByPhoneNumber(phone);

        if (info == null)
            throw new BusinessException(EmBusinessError.LOGIN_FAILURE);

        UserPassword password = userPasswordMapper.selectByUserId(info.getId());

        if (!password.getEncrpPswd().equals(psw))
            throw new BusinessException(EmBusinessError.LOGIN_FAILURE);

        UserModel model = aggregate(info, password);

        return model;
    }

    private UserPassword convertUM2Psw(UserModel userModel) {
        UserPassword psw = new UserPassword();

        psw.setEncrpPswd(userModel.getEncrptPassword());
        psw.setUserId(userModel.getId());

        return psw;

    }

    private UserInfo convertUM2Info(UserModel userModel) {
        UserInfo info = new UserInfo();

        BeanUtils.copyProperties(userModel, info);

        return info;
    }

    private UserModel aggregate(UserInfo info, UserPassword psw) {
        UserModel model = new UserModel();

        model.setAge(info.getAge());
        model.setId(info.getId());
        model.setGender(info.getGender());
        model.setPhone(info.getPhone());
        model.setRegisterMode(info.getRegisterMode());
        model.setThirdPartyId(info.getThirdPartyId());
        model.setName(info.getName());

        model.setEncrptPassword(psw.getEncrpPswd());

        return model;
    }
}
