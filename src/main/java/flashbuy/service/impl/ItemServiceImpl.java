package flashbuy.service.impl;

import flashbuy.dao.ItemDOMapper;
import flashbuy.dao.ItemStockDOMapper;
import flashbuy.dao.StockLogDOMapper;
import flashbuy.dataobject.ItemDO;
import flashbuy.dataobject.ItemStockDO;
import flashbuy.dataobject.StockLogDO;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.mq.MqProducer;
import flashbuy.service.ItemService;
import flashbuy.service.PromoService;
import flashbuy.service.model.ItemModel;
import flashbuy.service.model.PromoModel;
import flashbuy.validator.ValidationResult;
import flashbuy.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    public ItemDOMapper itemDOMapper;

    @Autowired
    public ItemStockDOMapper itemStockDOMapper;

    @Autowired
    public ValidatorImpl validator;

    @Autowired
    public PromoService promoService;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate template;

    @Autowired
    private MqProducer producer;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel model) throws BusinessException {

        ValidationResult result = validator.validate(model);

        if (result.isHasErrors())
            throw new BusinessException(EmBusinessError.INVALID_PARAMETER, result.getErrMsg());

        // 转化item model->data object
        ItemDO itemDo = this.convertItemDOFromItemModel(model);

        itemDOMapper.insertSelective(itemDo);
        model.setId(itemDo.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(model);

        itemStockDOMapper.insertSelective(itemStockDO);

        return this.getItemById(model.getId());
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        // 数据库行优化部分，扣库存都扔到缓存中完成
//        int affectedRow =  itemStockDOMapper.decreaseStock(itemId,amount);

        // result就是扣完的结果
        long result = template.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue() * -1);

        if(result > 0){
            return true;
        }else if (result == 0) {
            //打上库存已售罄的标识
            template.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");

            //更新库存成功
            return true;
        } else {
            //更新库存失败
            template.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
            return false;
        }
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        // 消息队列异步更新库存
        boolean mqResult = producer.asyncReduceStock(itemId, amount);
        return mqResult;
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        template.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        return true;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }

    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();

        stockLogDO.setAmount(amount);
        stockLogDO.setItemId(itemId);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);

        stockLogDOMapper.insertSelective(stockLogDO);

        return stockLogDO.getStockLogId();
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModels = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }
        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());


        //将dataobject->model
        ItemModel itemModel = convertModelFromDataObject(itemDO,itemStockDO);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) template.opsForValue().get("item_validate_"+id);
        if(itemModel == null){
            itemModel = this.getItemById(id);
            template.opsForValue().set("item_validate_"+id,itemModel);
            template.expire("item_validate_"+id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
