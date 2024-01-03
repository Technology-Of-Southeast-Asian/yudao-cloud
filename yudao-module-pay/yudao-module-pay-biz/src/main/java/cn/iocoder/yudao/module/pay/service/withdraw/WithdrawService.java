package cn.iocoder.yudao.module.pay.service.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawSaveReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.WithdrawDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * 提现订单
 * Service 接口
 *
 * @author 芋道源码
 */
public interface WithdrawService {

    /**
     * 创建提现订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWithdraw(@Valid WithdrawSaveReqVO createReqVO);

    /**
     * 更新提现订单
     *
     * @param updateReqVO 更新信息
     */
    void updateWithdraw(@Valid WithdrawSaveReqVO updateReqVO);

    /**
     * 删除提现订单
     *
     * @param id 编号
     */
    void deleteWithdraw(Long id);

    /**
     * 获得提现订单
     *
     * @param id 编号
     * @return 提现订单
     */
    WithdrawDO getWithdraw(Long id);

    /**
     * 获得提现订单
     * 分页
     *
     * @param pageReqVO 分页查询
     * @return 提现订单
     * 分页
     */
    PageResult<WithdrawDO> getWithdrawPage(WithdrawPageReqVO pageReqVO);


    /**
     * 提交支付
     * 此时，会发起支付渠道的调用
     *
     * @param reqVO  提交请求
     * @param userIp 提交 IP
     * @return 提交结果
     */
    PayWithdrawSubmitRespVO submitWithdraw(@Valid PayWithdrawSubmitReqVO reqVO,
                                           @NotEmpty(message = "提交 IP 不能为空") String userIp);

}