package com.ypat.third.wxlogin;

import org.apache.commons.lang.StringEscapeUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class WxUtils {
    private static Logger logger = LoggerFactory.getLogger(WxUtils.class);
    public static JSONObject getUserInfo(String encryptedData, String sessionKey, String iv){
        // 被加密的数据
        byte[] dataByte = Base64.decode(StringEscapeUtils.unescapeJava(encryptedData));
        // 加密秘钥
        byte[] keyByte = Base64.decode(StringEscapeUtils.unescapeJava(sessionKey));
        // 偏移量
        byte[] ivByte = Base64.decode(StringEscapeUtils.unescapeJava(iv));
        try {
            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                JSONObject jsonObject = new JSONObject(result);
                logger.info("获取微信加密数据："+result);
                return jsonObject;
            }
        } catch (Exception e) {
            logger.error("登录异常：", e);
            throw new RuntimeException("登录异常");
        }
        return null;
    }

    public static String wxDecrypt(String encrypted, String sessionKey, String iv) throws Exception {
        byte[] encrypData = Base64.decode(encrypted);
        byte[] ivData = Base64.decode(iv);
        byte[] sKey = Base64.decode(sessionKey);
        String decrypt = decrypt(sKey,ivData,encrypData);
        return decrypt;
    }

    public static String decrypt(byte[] key, byte[] iv, byte[] encData) throws Exception {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        //解析解密后的字符串
        return new String(cipher.doFinal(encData),"UTF-8");
    }

    public static void main(String[] args) {
        String data= "";
        String sess = "";
        String iv = "";
        WxUtils.getUserInfo(data,sess,iv);
    }
}
