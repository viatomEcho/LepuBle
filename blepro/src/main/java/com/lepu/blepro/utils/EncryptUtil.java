package com.lepu.blepro.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * AES的密钥长度，32字节，范围：16 - 32 字节
     */
    public static final int SECRET_KEY_LENGTH = 32;

    /**
     * 字符编码
     */
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    /**
     * 秘钥长度不足16个字节时，默认填充位数
     */
    private static final String DEFAULT_VALUE = "0";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    // key = "I am encrypt key"
    // LepuEncrypt(getAccessToken(), key);
    // LepuDecrypt(receiveBytes, key);
    // AESkey = 生成16位AES加密密钥(AccessToken校验通过后)
    // AesEncrypt(sendBytes, AESkey);
    // AesDecrypt(receiveBytes, AESkey);

    /**
     * MD5(lepucloud)
     * @return AccessToken前8个字节固定字符来源
     */
    public static byte[] getSecretKey() {
        byte[] secret = new byte[16];
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        digest.update("lepucloud".getBytes(StandardCharsets.UTF_8));
        byte[] key = digest.digest();
        System.arraycopy(key, 0, secret, 0, Math.min(16, key.length));
        return secret;
    }

    /**
     * 发送给设备待认证的内容
     * 前8个字节固定：MD5(lepucloud), getSecretKey()
     * 后四个字节：序号
     * @return AccessToken
     */
    public static byte[] getAccessToken(String id) {
        byte[] token = new byte[16];

        // 8 bytes 固定
        // 取 getSecretKey 一半
        for (int i =0; i < 8; i++) {
            token[i] = getSecretKey()[2*i];
        }

        // 4 bytes id
//        String id = "0001";
        System.arraycopy(id.getBytes(StandardCharsets.UTF_8), 0, token, 8, 4);

        // 4 bytes nonce
        long time = System.currentTimeMillis() / 1000;
        for (int i = 0; i<4; i++) {
            token[12+i] = (byte) (time >> i);
        }
        return token;
    }

    /**
     * 密钥交换数据加密
     * 数据按位异或加密
     * @param text 待加密数据
     * @param key 密钥
     */
    public static byte[] LepuEncrypt(byte[] text, byte[] key) {
        byte[] encrypted = new byte[text.length];
        for (int i =0; i<text.length; i++) {
            encrypted[i] = (byte) (text[i] ^ key[i%(key.length)]);
        }
        return encrypted;
    }

    /**
     * 密钥交换数据解密
     * 数据按位异或解密
     * @param text 待解密数据
     * @param key 密钥
     */
    public static byte[] LepuDecrypt(byte[] text, byte[] key) {
        byte[] decrypted = new byte[text.length];
        for (int i =0; i<text.length; i++) {
            decrypted[i] = (byte) (text[i] ^ key[i%(key.length)]);
        }
        return decrypted;
    }

    /**
     * 加密通讯数据加密
     * @param data 待加密数据
     * @param key AES密钥
     */
    public static byte[] AesEncrypt(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));
            byte[] encrypted = cipher.doFinal(data);
            return encrypted;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * 加密通讯数据解密
     * @param data 待解密数据
     * @param key AES密钥
     */
    public static byte[] AesDecrypt(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key));
            //执行解密操作
            byte[] result = cipher.doFinal(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static SecretKeySpec getSecretKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }
}
