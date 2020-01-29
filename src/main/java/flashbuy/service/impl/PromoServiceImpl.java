package flashbuy.service.impl;

import flashbuy.dao.PromoDOMapper;
import flashbuy.dataobject.PromoDO;
import flashbuy.service.PromoService;
import flashbuy.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        if (promoDO == null) return null;

        // data object -> model
        PromoModel promoModel = convertFromDataObject(promoDO);

        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) return null;
        PromoModel model = new PromoModel();

        BeanUtils.copyProperties(promoDO, model);

        // 有一些特殊字段需要特别的赋值一下
        model.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        model.setStartDate(new DateTime(promoDO.getStartDate()));
        model.setEndDate(new DateTime(promoDO.getEndDate()));

        return model;
    }
}
