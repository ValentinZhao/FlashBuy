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
}
