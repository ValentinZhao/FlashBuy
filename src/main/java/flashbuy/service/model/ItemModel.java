package flashbuy.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemModel implements Serializable {
    Integer id;

    // 商品名称
    String title;

    // 商品价格
    BigDecimal price;

    // 商品描述
    String description;

    // 商品库存，库存要分表来储存
    Integer stock;

    // 商品销量
    Integer sales;

    // 商品图片
    String imgUrl;

    //使用聚合模型,如果promoModel不为空，则表示其拥有还未结束的秒杀活动
    private PromoModel promoModel;

    public PromoModel getPromoModel() {
        return promoModel;
    }

    public void setPromoModel(PromoModel promoModel) {
        this.promoModel = promoModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
