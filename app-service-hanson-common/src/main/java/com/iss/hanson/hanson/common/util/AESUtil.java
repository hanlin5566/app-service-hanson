package com.iss.hanson.hanson.common.util;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESUtil {
    //使用AES-128-CBC加密模式，key需要为16位,key和iv可以相同，也可以不同!
    private static String KEY = "aaDJL2d9DfhLZO0z";
    private static String IV = "412ADDSSFA342442";
    private static final String CIPHER_ALGORITHM_CBC = "AES/CBC/NoPadding";
    /**
     * 加密方法 返回base64加密字符串
     * 和前端保持一致
     * @param data  要加密的数据
     * @param key 加密key
     * @param iv 加密iv
     * @return 加密的结果
     */
    public static String encrypt(String data, String key, String iv){
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);//"算法/模式/补码方式"NoPadding PKCS5Padding
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return Base64.encode(encrypted);
        } catch (Exception e) {
           log.error("encrypt failed",e);
            return null;
        }
    }

    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv 解密iv
     * @return 解密的结果
     */
    public static String decrypt(String data, String key, String iv){
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(Base64.decode(data));
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            log.error("decrypt failed",e);
            return null;
        }
    }

    /**
     * 使用默认的key和iv加密
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data){
        return encrypt(data, KEY, IV);
    }

    /**
     * 使用默认的key和iv解密
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrypt(String data){
        return decrypt(data, KEY, IV);
    }



    /**
     * 测试
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void main(String args[]) throws Exception {
        Map map = new HashMap<>();
        map.put("code", "0");
        map.put("data", "3232dsfs");
        String test1 = JSONObject.toJSONString(map);
        String test =new String(test1.getBytes(),"UTF-8");
        String data = null;
        data = encrypt(test);
        System.out.println("数据："+test);
        System.out.println("加密："+data);
        String jiemi =decrypt(data).trim();
        System.out.println("解密："+jiemi);

    }
}