package flashbuy.service.impl;

import flashbuy.dao.OrderInfoDOMapper;
import flashbuy.dao.SequenceInfoDOMapper;
import flashbuy.dataobject.OrderInfoDO;
import flashbuy.dataobject.SequenceInfoDO;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.service.ItemService;
import flashbuy.service.OrderService;
import flashbuy.service.UserService;
import flashbuy.service.model.ItemModel;
import flashbuy.service.model.OrderModel;
import flashbuy.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    OrderInfoDOMapper orderInfoDOMapper;

    @Autowired
    SequenceInfoDOMapper sequenceInfoDOMapper;

    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        //1.校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"商品信息不存在");
        }

        UserModel userModel = userService.getUserById(userId);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"用户信息不存在");
        }
        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"数量信息不正确");
        }

        if (promoId != null) {
            //（1）校验对应活动是否存在这个适用商品
            //（2）校验活动是否正在进行中
            if (!promoId.equals(itemModel.getPromoModel().getId())) {
                throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"活动信息不正确");
            } else if (itemModel.getPromoModel().getStatus() != 2) {
                throw new BusinessException(EmBusinessError.INVALID_PARAMETER,"活动未开始");
            }
        }


        //2.落单减库存
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);

        // 视活动信息来确认商品价格
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        OrderInfoDO orderInfoDO = convertFromOrderModel(orderModel);
        orderInfoDOMapper.insertSelective(orderInfoDO);

        //加上商品的销量
        itemService.increaseSales(itemId, amount);

        //4.返回前端

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNo() {
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        int sequence = 0;
        SequenceInfoDO sequenceInfoDO = sequenceInfoDOMapper.getSequenceByName("order_info");
        sequence = sequenceInfoDO.getCurrentValue();
        sequenceInfoDO.setCurrentValue(sequenceInfoDO.getCurrentValue() + sequenceInfoDO.getStep());
        sequenceInfoDOMapper.updateByPrimaryKeySelective(sequenceInfoDO);
        String sequenceStr = String.valueOf(sequence);

        for(int i = 0; i < 6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位,暂时写死
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderInfoDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderInfoDO orderDO = new OrderInfoDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setPromoId(orderModel.getPromoId());
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
