package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.Goods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsDao {

    /**
     * 获取商品集合
     * @return
     */
    List<Goods> getGoodsList();

    /**
     * 根据商品ID获取商品
     * @param goodsId 商品ID
     * @return
     */
    Goods getGoodsByID(String goodsId);

}
