package flashbuy.service;

import flashbuy.service.model.PromoModel;

public interface PromoService {

    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    public void publishPromo(Integer promoId);

    // 生成令牌
    public String getFlashbuyToken(Integer promoId,Integer itemId,Integer userId);
}
