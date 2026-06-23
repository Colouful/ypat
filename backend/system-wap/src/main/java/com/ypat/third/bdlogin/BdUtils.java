package com.ypat.third.bdlogin;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Arrays;

public class BdUtils {
    private static Logger logger = LoggerFactory.getLogger(BdUtils.class);
    private static Charset CHARSET = Charset.forName("utf-8");

    public static JSONObject getUserInfo(String encryptedData, String sessionKey) {
        byte [] aesKey = Base64.decodeBase64(sessionKey + "=");
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] encrypted = Base64.decodeBase64(encryptedData);
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            logger.error("登录异常：", e);
            throw new RuntimeException("登录异常");
        }
        String xmlContent;
        String fromClientId;
        try {
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);
            // 分离16位随机字符串,网络字节序和ClientId
            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);
            int xmlLength = recoverNetworkBytesOrder(networkOrder);
            xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET);
            fromClientId = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length), CHARSET);
            JSONObject jsonObject = new JSONObject(xmlContent);
            logger.info("获取百度加密数据："+xmlContent);
            return jsonObject;
        } catch (Exception e) {
            logger.error("登录异常：", e);
            throw new RuntimeException("登录异常");
        }
    }
    /**
     * 还原4个字节的网络字节序
     *
     * @param orderBytes 字节码
     *
     * @return sourceNumber
     */
    private static int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        int length = 4;
        int number = 8;
        for (int i = 0; i < length; i++) {
            sourceNumber <<= number;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    public static void main(String[] args) {
        String data = "";
        String sessionKey = "";
        JSONObject userInfo = BdUtils.getUserInfo(data, sessionKey);
        System.out.println(userInfo);
    }
}
