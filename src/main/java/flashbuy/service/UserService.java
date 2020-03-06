package flashbuy.service;

import flashbuy.error.BusinessException;
import flashbuy.service.model.UserModel;

public interface UserService {
    public UserModel getUserById(int id);

    public void register(UserModel userModel) throws BusinessException;

    //通过缓存获取用户对象
    public UserModel getUserByIdInCache(Integer id);

    public UserModel validateLogin(String phone, String psw) throws BusinessException;
}
