package cn.iocoder.yudao.framework.pay.core.client.impl.p51pay;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.client.util.HMACUtil;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.dromara.hutool.http.HttpUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.hutool.http.HttpUtil.decodeParamMap;

/**
 * 51pay
 *
 * @author jason
 */
@Slf4j
public class P51PayClient extends AbstractPayClient<P51PayClientConfig> {


    private static final String P_51PAY_RESP_SUCCESS_DATA = "P_51_PAY_SUCCESS";

    public P51PayClient(Long channelId, P51PayClientConfig config) {
        super(channelId, PayChannelEnum.P_51PAY.getCode(), config);
    }

    @Override
    @SneakyThrows
    protected void doInit() {
    }

    /**
     * 支付
     *
     * @param reqDTO
     * @return
     */
    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        //签名参数处理
        Map<String, Object> req = new HashMap<>();
        //商户编号  支付管理后台配置第三方支付商户号
        req.put("merchantNo", config.getMerchantNo());

        //商户订单号 唯一id 我方提供给第三方
        req.put("merchantOrderId", reqDTO.getOutTradeNo());

        //通道编码 第三方提供通道唯一编码
        req.put("channelCode", config.getChannelCode());

        //操作金额 以分为单位
        log.info("测试日志 ===============> 金额【{}】", reqDTO.getPrice());
        req.put("amount", reqDTO.getPrice());

        //货币代码 后台配置 INR
        req.put("currency", config.getCurrency());

        //支付人邮箱 前端提供
        String email = reqDTO.getChannelExtras().get("email");
        if (Strings.isNotBlank(email)) {
            req.put("email", email);
        }
        //支付人姓名 前端提供
        String userName = reqDTO.getChannelExtras().get("userName");
        if (Strings.isNotBlank(userName)) {
            req.put("userName", userName);
        }
        //手机好吗 支付人手机号 前端提供
        String mobileNo = reqDTO.getChannelExtras().get("mobileNo");
        if (Strings.isNotBlank(mobileNo)) {
            req.put("mobileNo", mobileNo);
        }
        //通知地址 后台配置
        req.put("notifyUrl", config.getNotifyUrl());

        //超时时间 后台配置
        req.put("expireTime", config.getExpireTime());
        String s = HMACUtil.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }

        //签名
        String sign = HMACUtil.sha256_HMAC(s, config.getKey());

        //非签名参数
        req.put("version", "1.0");

        //签名
        req.put("sign", sign);

        //交易描述 展示在收银台 后台配置
        req.put("subject", config.getSubject());
        //入金
        log.info("==================================>测试参数 第三方请求入参req{}", req);
        String post = HttpUtil.post(config.getAddress() + "/payin/unifiedorder.do", JsonUtils.toJsonString(req));
        log.info("==================================>测试参数 第三方请求返回参reqDTO{}", post);

        return PayOrderRespDTO.successOf("P-51PAY-" + reqDTO.getOutTradeNo(), "", LocalDateTime.now(),
                reqDTO.getOutTradeNo(), P_51PAY_RESP_SUCCESS_DATA);
    }

    /**
     * 支付回调
     *
     * @param params
     * @param body
     * @return
     */
    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body) throws Throwable {
// 1. 校验回调数据
        Map<String, String> bodyObj = decodeParamMap(body, StandardCharsets.UTF_8);
        //调用查询确定订单状态 判断订单存在且未关闭
        PayOrderRespDTO payOrderRespDTO = doGetOrder(bodyObj.get("merchantOrderId"));
        if (!ObjectUtil.isEmpty(payOrderRespDTO)) {
            // 2. 解析订单的状态
            Integer status = parseStatus(bodyObj.get("status"));
            Assert.notNull(status, (Supplier<Throwable>) () -> {
                throw new IllegalArgumentException(StrUtil.format("body({}) 的 status 不正确", body));
            });
            return PayOrderRespDTO.of(status, bodyObj.get("merchantOrderId"), "", parseTime(params.get("successTime")),
                    bodyObj.get("sysOrderId"), body);
        }
        throw new UnsupportedOperationException("模拟支付无支付回调");
    }

    private static Integer parseStatus(String tradeStatus) {
        return Objects.equals("NOTPAY", tradeStatus) ? PayOrderStatusRespEnum.WAITING.getStatus()
                : Objects.equals("SUCCESS", tradeStatus) ? PayOrderStatusRespEnum.SUCCESS.getStatus()
                : Objects.equals("CLOSED", tradeStatus) ? PayOrderStatusRespEnum.CLOSED.getStatus() : null;
    }

    /**
     * 查询支付结果
     *
     * @param outTradeNo
     * @return
     */
    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        //签名参数处理
        Map<String, Object> req = new HashMap<>();
        //商户编号
        req.put("merchantNo", config.getMerchantNo());
        //商户订单号
        req.put("merchantOrderId", outTradeNo);
        String s = HMACUtil.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACUtil.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);
//        String post = httpService.doPost(config.getAddress() + "/payin/queryorder.do", JsonUtils.toJsonString(req), "");
        String post = HttpUtil.post(config.getAddress() + "/payin/queryorder.do", JsonUtils.toJsonString(req));
        log.info("第三方返回结果:【{}】", post);
        return PayOrderRespDTO.successOf("P-51PAY-" + outTradeNo, "", LocalDateTime.now(),
                outTradeNo, P_51PAY_RESP_SUCCESS_DATA);
    }

    /**
     * 提现
     *
     * @param reqDTO
     * @return
     * @throws Throwable
     */
    @Override
    protected PayOrderRespDTO doUnifiedWithdraw(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        //签名参数处理
        Map<String, Object> req = new HashMap<>();
        //商户编号  支付管理后台配置第三方支付商户号
        req.put("merchantNo", config.getMerchantNo());

        //商户订单号 唯一id 我方提供给第三方
        req.put("merchantOrderId", reqDTO.getOutTradeNo());

        //通道编码 第三方提供通道唯一编码
        req.put("channelCode", config.getChannelCode());

        //操作金额 以分为单位
        req.put("amount", reqDTO.getPrice());

        //货币代码 后台配置 INR
        req.put("currency", config.getCurrency());

        //支付人邮箱 前端提供
        req.put("email", reqDTO.getChannelExtras().get("email"));
        //支付人姓名 前端提供
        req.put("userName", reqDTO.getChannelExtras().get("userName"));
        //手机好吗 支付人手机号 前端提供
        req.put("mobileNo", reqDTO.getChannelExtras().get("mobileNo"));

        //通知地址
        req.put("notifyUrl", config.getNotifyUrl());
        req.put("expireTime", config.getExpireTime());
        String s = HMACUtil.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACUtil.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);
        //银行信息
        req.put("bankInfo", "reqDTO.()");
        HttpUtil.post(config.getAddress() + "/payout/unifiedorder.do", JsonUtils.toJsonString(req));
        return PayOrderRespDTO.successOf("P-51PAY-" + reqDTO.getOutTradeNo(), "", LocalDateTime.now(),
                reqDTO.getOutTradeNo(), P_51PAY_RESP_SUCCESS_DATA);
    }

    /**
     * 提现回调
     *
     * @param params
     * @param body
     * @return
     * @throws Throwable
     */
    @Override
    protected PayOrderRespDTO doParseWithdrawNotify(Map<String, String> params, String body) throws Throwable {
        // 1. 校验回调数据
        Map<String, String> bodyObj = decodeParamMap(body, StandardCharsets.UTF_8);
        //调用查询确定订单状态
        PayOrderRespDTO payOrderRespDTO = doGetWithdraw(bodyObj.get("merchantOrderId"));
        if (!ObjectUtil.isEmpty(payOrderRespDTO)) {
            if ("SUCCESS".equals(payOrderRespDTO.getStatus()) || "NOTPAY".equals(payOrderRespDTO.getStatus())) {
                // 2. 解析订单的状态
                Integer status = parseStatus(bodyObj.get("status"));
                Assert.notNull(status, (Supplier<Throwable>) () -> {
                    throw new IllegalArgumentException(StrUtil.format("body({}) 的 status 不正确", body));
                });
                return PayOrderRespDTO.of(status, bodyObj.get("merchantOrderId"), "", parseTime(params.get("successTime")),
                        bodyObj.get("sysOrderId"), body);
            }
        }
        throw new UnsupportedOperationException("模拟支付无提现回调");
    }

    /**
     * 查询提现结果
     *
     * @param outTradeNo
     * @return
     * @throws Throwable
     */
    @Override
    protected PayOrderRespDTO doGetWithdraw(String outTradeNo) throws Throwable {
        //签名参数处理
        Map<String, Object> req = new HashMap<>();
        //商户编号
        req.put("merchantNo", config.getMerchantNo());
        //商户订单号
        req.put("merchantOrderId", outTradeNo);

        String s = HMACUtil.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACUtil.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);

        HttpUtil.post(config.getAddress() + "/payout/queryorder.do", JsonUtils.toJsonString(req));

        return PayOrderRespDTO.successOf("P-51PAY-" + outTradeNo, "", LocalDateTime.now(),
                outTradeNo, P_51PAY_RESP_SUCCESS_DATA);
    }


//
//    /**
//     * 出金下单
//     * @param reqDTO
//     * @return
//     */
//    protected PayOrderRespDTO doUnifiedOrderOut(PayOrderUnifiedReqDTO reqDTO) {
//
//        return PayOrderRespDTO.successOf("P-51PAY-" + reqDTO.getOutTradeNo(), "", LocalDateTime.now(),
//                reqDTO.getOutTradeNo(), P_51PAY_RESP_SUCCESS_DATA);
//    }
//
//    /**
//     * 出金查询
//     * @param outTradeNo
//     * @return
//     */
//    public PayOrderRespDTO doGetOrderOut(String outTradeNo) {
//
//        //签名参数处理
//        Map<String, Object> req = new HashMap<>(5);
//        //商户编号
//        req.put("merchantNo", config.getMerchantNo());
//        //商户订单号
//        req.put("merchantOrderId", outTradeNo);
//
//        String s = HMACSHA256.sortMap(req) + "key=" + config.getKey();
//        if (log.isDebugEnabled()) {
//            log.debug("签名参数==============>{}", s);
//        }
//        //签名
//        String sign = HMACSHA256.sha256_HMAC(s, config.getKey());
//        //非签名参数
//        req.put("version", config.getVersion());
//        //签名
//        req.put("sign", sign);
//
//        HttpUtil.post(config.getAddress() + "/payin/queryorder.do", JsonUtils.toJsonString(req));
//
//        return PayOrderRespDTO.successOf("P-51PAY-" + outTradeNo, "", LocalDateTime.now(),
//                outTradeNo, P_51PAY_RESP_SUCCESS_DATA);
//    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        return PayRefundRespDTO.successOf("MOCK-R-" + reqDTO.getOutRefundNo(), LocalDateTime.now(),
                reqDTO.getOutRefundNo(), P_51PAY_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        return PayRefundRespDTO.successOf("MOCK-R-" + outRefundNo, LocalDateTime.now(),
                outRefundNo, P_51PAY_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body) {
        throw new UnsupportedOperationException("模拟支付无退款回调");
    }

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) {
        throw new UnsupportedOperationException("待实现");
    }

    protected LocalDateTime parseTime(String str) {
        return LocalDateTimeUtil.parse(str, NORM_DATETIME_FORMATTER);
    }

}

