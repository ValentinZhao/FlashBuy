package flashbuy.controller;

import com.alibaba.druid.util.StringUtils;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.mq.MqProducer;
import flashbuy.response.CommonReturnType;
import flashbuy.service.ItemService;
import flashbuy.service.OrderService;
import flashbuy.service.PromoService;
import flashbuy.service.model.OrderModel;
import flashbuy.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    @PostMapping(path="/generatetoken", consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType generatetoken(@RequestParam(name="itemId") Integer itemId,
                                        @RequestParam(name="promoId", required = false) Integer promoId) throws BusinessException {
        String token = httpServletRequest.getParameterMap().get("token")[0];

        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);

        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //获取秒杀访问令牌
        String promoToken = promoService.getFlashbuyToken(promoId,itemId,userModel.getId());

        if(promoToken == null){
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"生成令牌失败");
        }
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    @PostMapping(path="/createorder", consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId") Integer itemId,
                                        @RequestParam(name="promoId", required = false) Integer promoId,
                                        @RequestParam(name="amount") Integer amount,
                                        @RequestParam(name="promoToken",required = false)String promoToken) throws BusinessException {
        OrderModel orderModel = new OrderModel();

//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

        String token = httpServletRequest.getParameterMap().get("token")[0];

        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);

        if(userModel == null){
                throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"秒杀令牌校验失败");
            }
        }

        // 检查是否已经售罄
        boolean res = redisTemplate.hasKey("promo_item_stock_invalid_"+itemId);
        if (res) throw new BusinessException(EmBusinessError.UNKNOWN_PARAMETER, "已经售罄！");

        // 初始化库存流水状态
        String stockLog = itemService.initStockLog(itemId, amount);

        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLog)) {
            throw new BusinessException(EmBusinessError.UNKNOWN_PARAMETER, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
