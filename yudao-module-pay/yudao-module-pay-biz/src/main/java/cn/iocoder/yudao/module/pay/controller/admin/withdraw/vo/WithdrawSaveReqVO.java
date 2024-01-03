package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 提现订单新增/修改 Request VO")
@Data
public class WithdrawSaveReqVO {

    @Schema(description = "提现订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "14921")
    private Long id;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6852")
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @Schema(description = "渠道编号", example = "28600")
    private Long channelId;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "商户订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19416")
    @NotEmpty(message = "商户订单编号不能为空")
    private String merchantOrderId;

    @Schema(description = "商品标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品标题不能为空")
    private String subject;

    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品描述不能为空")
    private String body;

    @Schema(description = "异步通知地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotEmpty(message = "异步通知地址不能为空")
    private String notifyUrl;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @NotNull(message = "提现金额，单位：分不能为空")
    private Long price;

    @Schema(description = "渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(description = "渠道手续金额，单位：分", example = "25484")
    private Long channelFeePrice;

    @Schema(description = "提现状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "提现状态不能为空")
    private Integer status;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "用户 IP不能为空")
    private String userIp;

    @Schema(description = "订单失效时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单失效时间不能为空")
    private LocalDateTime expireTime;

    @Schema(description = "订单提现成功时间")
    private LocalDateTime successTime;

    @Schema(description = "提现成功的订单拓展单编号", example = "19994")
    private Long extensionId;

    @Schema(description = "提现订单号")
    private String no;

    @Schema(description = "提现总金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "3218")
    @NotNull(message = "提现总金额，单位：分不能为空")
    private Long refundPrice;

    @Schema(description = "渠道用户编号", example = "24562")
    private String channelUserId;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

}