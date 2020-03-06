package flashbuy.service;

import flashbuy.error.BusinessException;
import flashbuy.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    public ItemModel createItem(ItemModel model) throws BusinessException;

    //商品列表浏览
    public List<ItemModel> listItem();

    //商品详情浏览
    public ItemModel getItemById(Integer id);

    //item及promo model缓存模型
    public ItemModel getItemByIdInCache(Integer id);

    // 减库存
    public boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException;

    // 异步减库存
    boolean asyncDecreaseStock(Integer itemId,Integer amount);

    // 库存回滚
    boolean increaseStock(Integer itemId,Integer amount);

    // 加销量
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException;

    // 初始化库存流水，主要是初始化事务开始
    public String initStockLog(Integer itemId, Integer amount);
}
