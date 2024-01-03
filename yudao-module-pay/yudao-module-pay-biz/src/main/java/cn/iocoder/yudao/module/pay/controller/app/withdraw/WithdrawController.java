package cn.iocoder.yudao.module.pay.controller.app.withdraw;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.WithdrawSaveReqVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppPayWithdrawSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppPayWithdrawSubmitRespVO;
import cn.iocoder.yudao.module.pay.convert.withdraw.PayWithdrawConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.WithdrawDO;
import cn.iocoder.yudao.module.pay.framework.pay.core.WalletPayClient;
import cn.iocoder.yudao.module.pay.service.withdraw.WithdrawService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserType;

@Tag(name = "管理后台 - 提现订单")
@RestController
@RequestMapping("/pay/withdraw")
@Validated
@Slf4j
public class WithdrawController {

    @Resource
    private WithdrawService withdrawService;

    @PostMapping("/create")
    @Operation(summary = "创建提现订单")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:create')")
    public CommonResult<Long> createWithdraw(@Valid @RequestBody WithdrawSaveReqVO createReqVO) {
        return success(withdrawService.createWithdraw(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新提现订单")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:update')")
    public CommonResult<Boolean> updateWithdraw(@Valid @RequestBody WithdrawSaveReqVO updateReqVO) {
        withdrawService.updateWithdraw(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除提现订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:withdraw:delete')")
    public CommonResult<Boolean> deleteWithdraw(@RequestParam("id") Long id) {
        withdrawService.deleteWithdraw(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得提现订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:query')")
    public CommonResult<WithdrawRespVO> getWithdraw(@RequestParam("id") Long id) {
        WithdrawDO withdraw = withdrawService.getWithdraw(id);
        return success(BeanUtils.toBean(withdraw, WithdrawRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得提现订单分页")
    @PreAuthorize("@ss.hasPermission('pay:withdraw:query')")
    public CommonResult<PageResult<WithdrawRespVO>> getWithdrawPage(@Valid WithdrawPageReqVO pageReqVO) {
        PageResult<WithdrawDO> pageResult = withdrawService.getWithdrawPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WithdrawRespVO.class));
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出提现订单 Excel")
//    @PreAuthorize("@ss.hasPermission('pay:withdraw:export')")
//    @OperateLog(type = EXPORT)
//    public void exportWithdrawExcel(@Valid WithdrawPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<WithdrawDO> list = withdrawService.getWithdrawPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "提现订单.xls", "数据", WithdrawRespVO.class,
//                        BeanUtils.toBean(list, WithdrawRespVO.class));
//    }

    @PostMapping("/submit")
    @Operation(summary = "提交支付订单")
    public CommonResult<AppPayWithdrawSubmitRespVO> submitPayWithdraw(@RequestBody AppPayWithdrawSubmitReqVO reqVO) {
        // 1. 钱包支付事，需要额外传 user_id 和 user_type
        if (Objects.equals(reqVO.getChannelCode(), PayChannelEnum.WALLET.getCode())) {
            Map<String, String> channelExtras = reqVO.getChannelExtras() == null ?
                    Maps.newHashMapWithExpectedSize(2) : reqVO.getChannelExtras();
            channelExtras.put(WalletPayClient.USER_ID_KEY, String.valueOf(getLoginUserId()));
            channelExtras.put(WalletPayClient.USER_TYPE_KEY, String.valueOf(getLoginUserType()));
            reqVO.setChannelExtras(channelExtras);
        }
        // 2. 提交支付
        PayWithdrawSubmitRespVO respVO = withdrawService.submitWithdraw(reqVO, getClientIP());

        return success(PayWithdrawConvert.INSTANCE.convert3(respVO));
    }

}