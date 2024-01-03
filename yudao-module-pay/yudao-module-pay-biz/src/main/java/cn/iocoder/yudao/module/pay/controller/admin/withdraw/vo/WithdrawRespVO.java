package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 提现订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WithdrawRespVO {

    @Schema(description = "提现订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "14921")
    @ExcelProperty("提现订单编号")
    private Long id;

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6852")
    @ExcelProperty("应用编号")
    private Long appId;

    @Schema(description = "渠道编号", example = "28600")
    @ExcelProperty("渠道编号")
    private Long channelId;

    @Schema(description = "渠道编码")
    @ExcelProperty("渠道编码")
    private String channelCode;

    @Schema(description = "商户订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19416")
    @ExcelProperty("商户订单编号")
    private String merchantOrderId;

    @Schema(description = "商品标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("商品标题")
    private String subject;

    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("商品描述")
    private String body;

    @Schema(description = "异步通知地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("异步通知地址")
    private String notifyUrl;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1204")
    @ExcelProperty("提现金额，单位：分")
    private Long price;

    @Schema(description = "渠道手续费，单位：百分比")
    @ExcelProperty("渠道手续费，单位：百分比")
    private Double channelFeeRate;

    @Schema(description = "渠道手续金额，单位：分", example = "25484")
    @ExcelProperty("渠道手续金额，单位：分")
    private Long channelFeePrice;

    @Schema(description = "提现状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "提现状态", converter = DictConvert.class)
    @DictFormat("pay_transfer_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("用户 IP")
    private String userIp;

    @Schema(description = "订单失效时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "订单提现成功时间")
    @ExcelProperty("订单提现成功时间")
    private LocalDateTime successTime;

    @Schema(description = "提现成功的订单拓展单编号", example = "19994")
    @ExcelProperty("提现成功的订单拓展单编号")
    private Long extensionId;

    @Schema(description = "提现订单号")
    @ExcelProperty("提现订单号")
    private String no;

    @Schema(description = "提现总金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "3218")
    @ExcelProperty("提现总金额，单位：分")
    private Long refundPrice;

    @Schema(description = "渠道用户编号", example = "24562")
    @ExcelProperty("渠道用户编号")
    private String channelUserId;

    @Schema(description = "渠道订单号")
    @ExcelProperty("渠道订单号")
    private String channelOrderNo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}