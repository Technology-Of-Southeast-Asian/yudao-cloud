package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 提现订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WithdrawPageReqVO extends PageParam {

    @Schema(description = "应用编号", example = "6852")
    private Long appId;

    @Schema(description = "渠道编号", example = "28600")
    private Long channelId;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "商户订单编号", example = "19416")
    private String merchantOrderId;

    @Schema(description = "商品标题")
    private String subject;

    @Schema(description = "商品描述")
    private String body;

    @Schema(description = "异步通知地址", example = "https://www.iocoder.cn")
    private String notifyUrl;

    @Schema(description = "提现金额，单位：分", example = "1204")
    private Long price;

    @Schema(description = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(description = "渠道手续金额，单位：分", example = "25484")
    private Long channelFeePrice;

    @Schema(description = "提现状态", example = "1")
    private Integer status;

    @Schema(description = "用户 IP")
    private String userIp;

    @Schema(description = "订单失效时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireTime;

    @Schema(description = "订单提现成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] successTime;

    @Schema(description = "提现成功的订单拓展单编号", example = "19994")
    private Long extensionId;

    @Schema(description = "提现订单号")
    private String no;

    @Schema(description = "提现总金额，单位：分", example = "3218")
    private Long refundPrice;

    @Schema(description = "渠道用户编号", example = "24562")
    private String channelUserId;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}