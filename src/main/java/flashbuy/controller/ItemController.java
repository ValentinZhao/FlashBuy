package flashbuy.controller;

import flashbuy.controller.viewobject.ItemVO;
import flashbuy.dataobject.ItemDO;
import flashbuy.error.BusinessException;
import flashbuy.response.CommonReturnType;
import flashbuy.service.ItemService;
import flashbuy.service.model.ItemModel;
import flashbuy.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    @Qualifier("itemServiceImpl")
    private ItemService service;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate template;

    @PostMapping(path = "/create", consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel model = service.createItem(itemModel);

        ItemVO itemVO = convertVOFromModel(model);

        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name="id") Integer id) {
        // 先用redis查一次
        ItemModel model = (ItemModel) template.opsForValue().get("item_"+id);

        // 如果缓存没有就去数据库拿
        if (model == null) {
            model = service.getItemById(id);
            template.opsForValue().set("item_"+id, model);
            template.expire("item_"+id, 10, TimeUnit.MINUTES);
        }


        ItemVO itemVO = convertVOFromModel(model);

        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/list")
    @ResponseBody
    public CommonReturnType getItem() {
        List<ItemModel> itemModelList = service.listItem();

        //使用stream apiJ将list内的itemModel转化为ITEMVO;
        List<ItemVO> itemVOList =  itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);

        if (itemModel.getPromoModel() != null) {
            PromoModel promoModel = itemModel.getPromoModel();
            itemVO.setPromoId(promoModel.getId());
            itemVO.setPromoPrice(promoModel.getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoStatus(promoModel.getStatus());
        } else {
            itemVO.setPromoStatus(0);
        }

        return itemVO;
    }
}
