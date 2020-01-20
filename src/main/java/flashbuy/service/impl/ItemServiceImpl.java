package flashbuy.service.impl;

import flashbuy.dao.ItemDOMapper;
import flashbuy.dao.ItemStockDOMapper;
import flashbuy.dataobject.ItemDO;
import flashbuy.dataobject.ItemStockDO;
import flashbuy.error.BusinessException;
import flashbuy.error.EmBusinessError;
import flashbuy.service.ItemService;
import flashbuy.service.model.ItemModel;
import flashbuy.validator.ValidationResult;
import flashbuy.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    public ItemDOMapper itemDOMapper;

    @Autowired
    public ItemStockDOMapper itemStockDOMapper;

    @Autowired
    public ValidatorImpl validator;

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
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModels = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return null;
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
