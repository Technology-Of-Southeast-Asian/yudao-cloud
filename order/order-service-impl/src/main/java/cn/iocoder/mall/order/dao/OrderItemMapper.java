package cn.iocoder.mall.order.dao;

import cn.iocoder.mall.order.dataobject.OrderItemDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 订单 item mapper
 *
 * @author Sin
 * @time 2019-03-16 15:09
 */
@Repository
public interface OrderItemMapper {

    /**
     * 插入数据
     *
     * @param orderItemDO
     */
    void insert(OrderItemDO orderItemDO);

    /**
     * 更新 - 根据Id
     *
     * @param orderItemDO
     */
    void updateById(OrderItemDO orderItemDO);

    /**
     * 更新 - 根据Ids
     *
     * @param ids
     * @param orderItemDO
     */
    void updateByIds(
            @Param("ids") List<Integer> ids,
            OrderItemDO orderItemDO
    );

    /**
     * 查询 - 根据 orderId 下的 item
     *
     * @param orderId
     * @return
     */
    List<OrderItemDO> selectByOrderIdAndDeleted(
            @Param("orderId") Integer orderId,
            @Param("deleted") @NotNull Integer deleted
    );
}