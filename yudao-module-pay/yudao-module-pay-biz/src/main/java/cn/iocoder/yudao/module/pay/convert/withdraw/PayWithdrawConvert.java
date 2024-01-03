package cn.iocoder.yudao.module.pay.convert.withdraw;

import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawSubmitRespVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppPayWithdrawSubmitRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 支付订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayWithdrawConvert {

    PayWithdrawConvert INSTANCE = Mappers.getMapper(PayWithdrawConvert.class);

//    PayWithdrawRespVO convert(PayWithdrawDO bean);
//
//    PayWithdrawRespDTO convert2(PayWithdrawDO Withdraw);
//
//    default PayWithdrawDetailsRespVO convert(PayWithdrawDO Withdraw, PayWithdrawExtensionDO WithdrawExtension, PayAppDO app) {
//        PayWithdrawDetailsRespVO respVO = convertDetail(Withdraw);
//        respVO.setExtension(convert(WithdrawExtension));
//        if (app != null) {
//            respVO.setAppName(app.getName());
//        }
//        return respVO;
//    }
//    PayWithdrawDetailsRespVO convertDetail(PayWithdrawDO bean);
//    PayWithdrawDetailsRespVO.PayWithdrawExtension convert(PayWithdrawExtensionDO bean);
//
//    default PageResult<PayWithdrawPageItemRespVO> convertPage(PageResult<PayWithdrawDO> page, Map<Long, PayAppDO> appMap) {
//        PageResult<PayWithdrawPageItemRespVO> result = convertPage(page);
//        result.getList().forEach(Withdraw -> MapUtils.findAndThen(appMap, Withdraw.getAppId(), app -> Withdraw.setAppName(app.getName())));
//        return result;
//    }
//    PageResult<PayWithdrawPageItemRespVO> convertPage(PageResult<PayWithdrawDO> page);
//
//    default List<PayWithdrawExcelVO> convertList(List<PayWithdrawDO> list, Map<Long, PayAppDO> appMap) {
//        return CollectionUtils.convertList(list, Withdraw -> {
//            PayWithdrawExcelVO excelVO = convertExcel(Withdraw);
//            MapUtils.findAndThen(appMap, Withdraw.getAppId(), app -> excelVO.setAppName(app.getName()));
//            return excelVO;
//        });
//    }
//    PayWithdrawExcelVO convertExcel(PayWithdrawDO bean);
//
//    PayWithdrawDO convert(PayWithdrawCreateReqDTO bean);
//
//    @Mapping(target = "id", ignore = true)
//    PayWithdrawExtensionDO convert(PayWithdrawSubmitReqVO bean, String userIp);
//
//    PayWithdrawUnifiedReqDTO convert2(PayWithdrawSubmitReqVO reqVO, String userIp);
//
//    @Mapping(source = "Withdraw.status", target = "status")
//    PayWithdrawSubmitRespVO convert(PayWithdrawDO Withdraw, cn.iocoder.yudao.framework.pay.core.client.dto.Withdraw.PayWithdrawRespDTO respDTO);

    AppPayWithdrawSubmitRespVO convert3(PayWithdrawSubmitRespVO bean);

}
