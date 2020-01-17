package flashbuy.service.impl;

import flashbuy.dao.UserInfoMapper;
import flashbuy.dao.UserPasswordMapper;
import flashbuy.dataobject.UserInfo;
import flashbuy.dataobject.UserPassword;
import flashbuy.service.UserService;
import flashbuy.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserPasswordMapper userPasswordMapper;

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
