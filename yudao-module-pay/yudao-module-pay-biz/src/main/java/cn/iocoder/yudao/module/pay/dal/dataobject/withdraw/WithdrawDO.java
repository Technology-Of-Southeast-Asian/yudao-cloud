package cn.iocoder.yudao.module.pay.dal.dataobject.withdraw;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 提现订单
 * DO
 *
 * @author 芋道源码
 */
@TableName("pay_withdraw")
@KeySequence("pay_withdraw_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawDO extends BaseDO {

    /**
     * 提现订单编号
     */
    @TableId
    private Long id;
    /**
     * 应用编号
     */
    private Long appId;
    /**
     * 渠道编号
     */
    private Long channelId;
    /**
     * 渠道编码
     */
    private String channelCode;
    /**
     * 商户订单编号
     */
    private String merchantOrderId;
    /**
     * 商品标题
     */
    private String subject;
    /**
     * 商品描述
     */
    private String body;
    /**
     * 异步通知地址
     */
    private String notifyUrl;
    /**
     * 提现金额，单位：分
     */
    private Long price;
    /**
     * 渠道手续费，单位：百分比
     */
    private Double channelFeeRate;
    /**
     * 渠道手续金额，单位：分
     */
    private Long channelFeePrice;
    /**
     * 提现状态
     * <p>
     * 枚举 {@link TODO pay_transfer_type 对应的类}
     */
    private Integer status;
    /**
     * 用户 IP
     */
    private String userIp;
    /**
     * 订单失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 订单提现成功时间
     */
    private LocalDateTime successTime;
    /**
     * 提现成功的订单拓展单编号
     */
    private Long extensionId;
    /**
     * 提现订单号
     */
    private String no;
    /**
     * 提现总金额，单位：分
     */
    private Long refundPrice;
    /**
     * 渠道用户编号
     */
    private String channelUserId;
    /**
     * 渠道订单号
     */
    private String channelOrderNo;

}