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
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.http.HttpUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

/**
 * 51pay
 *
 * @author jason
 */
@Slf4j
public class P51PayClient extends AbstractPayClient<P51PayClientConfig> {

    private static final String P_51PAY_RESP_SUCCESS_DATA = "P_51PAY_SUCCESS";

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
        //商户编号
        req.put("merchantNo", config.getMerchantNo());
        //商户订单号
        req.put("merchantOrderId", reqDTO.getOutTradeNo());
        req.put("channelCode", config.getChannelCode());
        req.put("amount", reqDTO.getPrice());
        req.put("currency", config.getCurrency());
        req.put("email", config.getEmail());
        req.put("userName", config.getUserName());
        req.put("mobileNo", config.getMobileNo());
        //通知地址
        req.put("notifyUrl", config.getNotifyUrl());
        req.put("expireTime", config.getExpireTime());
        String s = HMACSHA256.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACSHA256.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);
        //交易描述
        req.put("subject", config.getSubject());
        //入金
        HttpUtil.post(config.getAddress() + "/payin/unifiedorder.do", JsonUtils.toJsonString(req));
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
        Map<String, String> bodyObj = cn.hutool.http.HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        //调用查询确定订单状态

        PayOrderRespDTO payOrderRespDTO = doGetOrder(bodyObj.get("merchantOrderId"));
        if (ObjectUtil.isEmpty(payOrderRespDTO) || "SUCCESS".equals(payOrderRespDTO.getStatus()) || "NOTPAY".equals(payOrderRespDTO.getStatus())) {

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
        String s = HMACSHA256.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACSHA256.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);
        HttpUtil.post(config.getAddress() + "/payin/queryorder.do", JsonUtils.toJsonString(req));
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
        //商户编号
        req.put("merchantNo", config.getMerchantNo());
        //商户订单号
        req.put("merchantOrderId", reqDTO.getOutTradeNo());
        req.put("channelCode", config.getChannelCode());
        req.put("amount", reqDTO.getPrice());
        req.put("currency", config.getCurrency());
        req.put("email", config.getEmail());
        req.put("userName", config.getUserName());
        req.put("mobileNo", config.getMobileNo());
        //通知地址
        req.put("notifyUrl", config.getNotifyUrl());
        req.put("expireTime", config.getExpireTime());
        String s = HMACSHA256.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACSHA256.sha256_HMAC(s, config.getKey());
        //非签名参数
        req.put("version", config.getVersion());
        //签名
        req.put("sign", sign);
        //银行信息
        req.put("bankInfo", config.getVersion());
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
        Map<String, String> bodyObj = cn.hutool.http.HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
        //调用查询确定订单状态
        PayOrderRespDTO payOrderRespDTO = doGetWithdraw(bodyObj.get("merchantOrderId"));
        if (ObjectUtil.isEmpty(payOrderRespDTO) || "SUCCESS".equals(payOrderRespDTO.getStatus()) || "NOTPAY".equals(payOrderRespDTO.getStatus())) {
            // 2. 解析订单的状态
            Integer status = parseStatus(bodyObj.get("status"));
            Assert.notNull(status, (Supplier<Throwable>) () -> {
                throw new IllegalArgumentException(StrUtil.format("body({}) 的 status 不正确", body));
            });
            ;
            return PayOrderRespDTO.of(status, bodyObj.get("merchantOrderId"), "", parseTime(params.get("successTime")),
                    bodyObj.get("sysOrderId"), body);
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

        String s = HMACSHA256.sortMap(req) + "key=" + config.getKey();
        if (log.isDebugEnabled()) {
            log.debug("签名参数==============>{}", s);
        }
        //签名
        String sign = HMACSHA256.sha256_HMAC(s, config.getKey());
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


class HMACSHA256 {
    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    public static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    /**
     * 字节数组转16进制
     *
     * @param b .
     * @return .
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }

            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * 对map进行key的升序排列
     *
     * @param map
     * @return
     */
    public static String sortMap(Map<String, Object> map) {
        List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(map.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {
            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });
        StringBuffer params = new StringBuffer();
        for (int i = 0; i < infoIds.size(); i++) {
            String id = infoIds.get(i).toString();
            params.append(id).append("&");
        }
        return params.toString();
    }

    public static void main(String[] args) {

        String str = "amount=10000&channelCode=A001&currency=INR&expireTime=60&merchantNo=M211105002038019071&merchantOrderId=20211111160100001&mobileNo=15982360145&notifyUrl=https://www.baidu.com/&key=5daea29d7969408984117dd853675ba5";
        String s = "balanceType=IN&currency=INR&merchantNo=M231221170850008305&version=1.0&&key=57f179e69c934dc18fcb8d9103c6ac1e";
        String sha256_hmac = sha256_HMAC(str, "5daea29d7969408984117dd853675ba5");
        System.out.println(sha256_hmac);
    }
}