package cn.iocoder.yudao.module.pay.service.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawSaveReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.WithdrawDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderExtensionMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.withdraw.WithdrawMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 提现订单
 * Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class WithdrawServiceImpl implements WithdrawService {

    @Resource
    private WithdrawMapper withdrawMapper;

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayOrderExtensionMapper orderExtensionMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public Long createWithdraw(WithdrawSaveReqVO createReqVO) {
        // 插入
        WithdrawDO withdraw = BeanUtils.toBean(createReqVO, WithdrawDO.class);
        withdrawMapper.insert(withdraw);
        // 返回
        return withdraw.getId();
    }

    @Override
    public void updateWithdraw(WithdrawSaveReqVO updateReqVO) {
        // 校验存在
        validateWithdrawExists(updateReqVO.getId());
        // 更新
        WithdrawDO updateObj = BeanUtils.toBean(updateReqVO, WithdrawDO.class);
        withdrawMapper.updateById(updateObj);
    }

    @Override
    public void deleteWithdraw(Long id) {
        // 校验存在
        validateWithdrawExists(id);
        // 删除
        withdrawMapper.deleteById(id);
    }

    private void validateWithdrawExists(Long id) {
        if (withdrawMapper.selectById(id) == null) {
            throw exception(WITHDRAW_NOT_EXISTS);
        }
    }

    @Override
    public WithdrawDO getWithdraw(Long id) {
        return withdrawMapper.selectById(id);
    }

    @Override
    public PageResult<WithdrawDO> getWithdrawPage(WithdrawPageReqVO pageReqVO) {
        return withdrawMapper.selectPage(pageReqVO);
    }

    @Override // 注意，这里不能添加事务注解，避免调用支付渠道失败时，将 PayOrderExtensionDO 回滚了
    public PayWithdrawSubmitRespVO submitWithdraw(PayWithdrawSubmitReqVO reqVO, String userIp) {
        WithdrawDO order = validateOrderCanSubmit(reqVO.getId());
        PayChannelDO channel = validateChannelCanSubmit(order.getAppId(), reqVO.getChannelCode());

        WithdrawDO withdrawDO = new WithdrawDO();
        withdrawMapper.insert(withdrawDO);
        PayClient client = channelService.getPayClient(channel.getId());
// TODO 提款
//        PayWithDrawUnifiedReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqVO, userIp)
//                // 商户相关的字段
//                .setOutTradeNo(orderExtension.getNo()) // 注意，此处使用的是 PayOrderExtensionDO.no 属性！
//                .setSubject(order.getSubject()).setBody(order.getBody())
//                .setNotifyUrl(genChannelOrderNotifyUrl(channel))
//                .setReturnUrl(reqVO.getReturnUrl())
//                // 订单相关字段
//                .setPrice(order.getPrice()).setExpireTime(order.getExpireTime());
//
//        client.unifiedWithdraw()

        return new PayWithdrawSubmitRespVO();
    }

    private PayChannelDO validateChannelCanSubmit(Long appId, String channelCode) {
        // 校验 App
        appService.validPayApp(appId);
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(appId, channelCode);
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validatePayChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    private WithdrawDO validateOrderCanSubmit(Long id) {
        WithdrawDO withdrawDO = withdrawMapper.selectById(id);
        if (withdrawDO == null) { // 是否存在
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(withdrawDO.getStatus())) { // 校验状态，发现已支付
            throw exception(PAY_ORDER_STATUS_IS_SUCCESS);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(withdrawDO.getStatus())) { // 校验状态，必须是待支付
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (LocalDateTimeUtils.beforeNow(withdrawDO.getExpireTime())) { // 校验是否过期
            throw exception(PAY_ORDER_IS_EXPIRED);
        }

        // 【重要】校验是否支付拓展单已支付，只是没有回调、或者数据不正常
//        validateOrderActuallyPaid(id);
        return withdrawDO;
    }

    /**
     * 校验支付订单实际已支付
     *
     * @param id 支付编号
     */
//    @VisibleForTesting
//    void validateOrderActuallyPaid(Long id) {
//        List<PayOrderExtensionDO> orderExtensions = orderExtensionMapper.selectListByOrderId(id);
//        orderExtensions.forEach(orderExtension -> {
//            // 情况一：校验数据库中的 orderExtension 是不是已支付
//            if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
//                log.warn("[validateOrderCanSubmit][order({}) 的 extension({}) 已支付，可能是数据不一致]",
//                        id, orderExtension.getId());
//                throw exception(PAY_ORDER_EXTENSION_IS_PAID);
//            }
//            // 情况二：调用三方接口，查询支付单状态，是不是已支付
//            PayClient payClient = channelService.getPayClient(orderExtension.getChannelId());
//            if (payClient == null) {
//                log.error("[validateOrderCanSubmit][渠道编号({}) 找不到对应的支付客户端]", orderExtension.getChannelId());
//                return;
//            }
//            PayOrderRespDTO respDTO = payClient.getOrder(orderExtension.getNo());
//            if (respDTO != null && PayOrderStatusRespEnum.isSuccess(respDTO.getStatus())) {
//                log.warn("[validateOrderCanSubmit][order({}) 的 PayOrderRespDTO({}) 已支付，可能是回调延迟]",
//                        id, toJsonString(respDTO));
//                throw exception(PAY_ORDER_EXTENSION_IS_PAID);
//            }
//        });
//    }


}