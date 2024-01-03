package cn.iocoder.yudao.module.pay.controller.app.withdraw.vo;

import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 提现订单提交 Request VO")
@Data
public class AppPayWithdrawSubmitReqVO extends PayWithdrawSubmitReqVO {
}
