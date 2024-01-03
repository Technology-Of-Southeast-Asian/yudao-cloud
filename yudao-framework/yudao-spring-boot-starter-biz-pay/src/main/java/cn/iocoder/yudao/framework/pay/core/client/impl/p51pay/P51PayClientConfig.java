package cn.iocoder.yudao.framework.pay.core.client.impl.p51pay;

import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import jakarta.validation.Validator;
import lombok.Data;

/**
 * 51pay
 *
 * @author jason
 */
@Data
public class P51PayClientConfig implements PayClientConfig {


    /**
     * api地址
     */
    private String address = "https://api.eggoout.com";


    /**
     * 签名加密key
     */
    private String key = "57f179e69c934dc18fcb8d9103c6ac1e";

    /**
     * 商户编号
     * 必传	签名
     * 从商户后台-商户信息中获取
     */
    private String merchantNo;

    /**
     * 版本号
     * 必传	不签名
     * 固定传：1.0
     */
    private String version;

    /**
     * 商户订单号
     * 必传	签名
     * 要求同一商户下此单号唯一
     */
    private String merchantOrderId;
    /**
     * 通道编码
     * 必传	签名
     * 商户后台被分配的通道列表中获取
     */

    private String channelCode;
    /**
     * 收款金额以分为单位
     * 必传	签名
     * 金额 * 100
     * 保留整数
     */
    private String Integer;
    /**
     * 货币代码
     * 必传	签名
     * 例如：INR
     * 查看所有支持的货币代码
     */
    private String currency;
    /**
     * 交易描述
     * 必传	不签名
     * 在收银台中展示
     * 不长于64位
     */
    private String subject;
    /**
     * 支付人邮箱
     * 必传	签名
     */
    private String email;
    /**
     * 支付人姓名
     * 、必传	签名
     */
    private String userName;
    /**
     * 支付人手机号
     * 必传	签名
     */
    private String mobileNo;
    /**
     * 支付人银行卡信息
     * 非必传	不签名
     * 格式为JSON字符串
     * 查看参数规则
     */
    private String bankInfo;
    /**
     * 收通知地址
     * 非必传	签名
     */
    private String notifyUrl;
    /**
     * 订单超时时间
     * 必传	签名
     * 超时平台会关闭订单
     * 单位：分钟
     * 最大710
     */
    private Integer expireTime;
    /**
     * 备注信息
     * 非必传	不签名
     */
    private String remark;
    /**
     * 扩展参数
     * 非必传	不签名
     * 格式为JSON字符串
     */
    private String extInfo;
    /**
     * 签名
     * 必传	不签名
     * 查看签名规则
     */
    private String sign;


    @Override
    public void validate(Validator validator) {
        // 模拟支付配置无需校验
    }

}
