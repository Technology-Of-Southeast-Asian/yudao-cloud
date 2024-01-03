package cn.iocoder.yudao.module.pay.dal.mysql.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.WithdrawDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提现订单
 * Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WithdrawMapper extends BaseMapperX<WithdrawDO> {

    default PageResult<WithdrawDO> selectPage(WithdrawPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WithdrawDO>()
                .eqIfPresent(WithdrawDO::getAppId, reqVO.getAppId())
                .eqIfPresent(WithdrawDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(WithdrawDO::getChannelCode, reqVO.getChannelCode())
                .eqIfPresent(WithdrawDO::getMerchantOrderId, reqVO.getMerchantOrderId())
                .eqIfPresent(WithdrawDO::getSubject, reqVO.getSubject())
                .eqIfPresent(WithdrawDO::getBody, reqVO.getBody())
                .eqIfPresent(WithdrawDO::getNotifyUrl, reqVO.getNotifyUrl())
                .eqIfPresent(WithdrawDO::getPrice, reqVO.getPrice())
                .eqIfPresent(WithdrawDO::getChannelFeeRate, reqVO.getChannelFeeRate())
                .eqIfPresent(WithdrawDO::getChannelFeePrice, reqVO.getChannelFeePrice())
                .eqIfPresent(WithdrawDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WithdrawDO::getUserIp, reqVO.getUserIp())
                .betweenIfPresent(WithdrawDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(WithdrawDO::getSuccessTime, reqVO.getSuccessTime())
                .eqIfPresent(WithdrawDO::getExtensionId, reqVO.getExtensionId())
                .eqIfPresent(WithdrawDO::getNo, reqVO.getNo())
                .eqIfPresent(WithdrawDO::getRefundPrice, reqVO.getRefundPrice())
                .eqIfPresent(WithdrawDO::getChannelUserId, reqVO.getChannelUserId())
                .eqIfPresent(WithdrawDO::getChannelOrderNo, reqVO.getChannelOrderNo())
                .betweenIfPresent(WithdrawDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WithdrawDO::getId));
    }

}