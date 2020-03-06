package flashbuy.controller;

import com.alibaba.druid.util.StringUtils;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.mq.MqProducer;
import flashbuy.response.CommonReturnType;
import flashbuy.service.ItemService;
import flashbuy.service.OrderService;
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

    @PostMapping(path="/createorder", consumes = {"application/x-www-form-urlencoded"})
    public CommonReturnType createOrder(@RequestParam(name="itemId") Integer itemId,
                                        @RequestParam(name="promoId", required = false) Integer promoId,
                                        @RequestParam(name="amount") Integer amount) throws BusinessException {
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

//        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

//        orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        // 检查是否已经售罄
        boolean res = redisTemplate.hasKey("promo_item_stock_invalid_"+itemId);
        if (!res) throw new BusinessException(EmBusinessError.UNKNOWN_PARAMETER, "已经售罄！");

        // 初始化库存流水状态
        String stockLog = itemService.initStockLog(itemId, amount);

        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLog)) {
            throw new BusinessException(EmBusinessError.UNKNOWN_PARAMETER, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
