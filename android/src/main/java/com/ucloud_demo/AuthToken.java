package com.ucloud_demo;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthToken {
    // 此方法生成token
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String generateToken(String uid, String roomid, String appid, String seckey){
        String token = "";
        try {
            String headerjson = "{" + "\"user_id\""+":"+ "\"" +uid  +"\""+","+ "\"room_id\""+":"+ "\""+ roomid+ "\""+","+ "\"app_id\"" +":"+ "\""+ appid + "\""+ "}" ;

            final Base64.Encoder encoder = Base64.getEncoder();
            final byte[] textByte = headerjson.getBytes("UTF-8");
            final String base64header = encoder.encodeToString(textByte);
            long rannum = System.currentTimeMillis()/1000;
            long times = System.currentTimeMillis()/1000;

            String sign = generate(uid, appid, seckey,roomid, (int)times, (int)rannum);
            token = base64header+"."+sign ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token ;
    }

    public static String generate(String uID,String appID, String appCertificate, String roomID, int unixTs, int randomInt) throws Exception {
        String unixTsStr = ("0000000000" + Integer.toString(unixTs)).substring(Integer.toString(unixTs).length());
        String randomIntStr = ("00000000" + Integer.toHexString(randomInt)).substring(Integer.toHexString(randomInt).length());
        String signature = generateSignature(uID,appID, appCertificate, roomID, unixTsStr, randomIntStr);
        return String.format("%s%s%s", signature, unixTsStr, randomIntStr);
    }

    private static String generateSignature(String uID,String appID, String appCertificate, String roomID, String unixTsStr, String randomIntStr) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(uID.getBytes());
        baos.write(appID.getBytes());
        baos.write(unixTsStr.getBytes());
        baos.write(randomIntStr.getBytes());
        baos.write(roomID.getBytes());
        byte[] sign = encodeHMAC(appCertificate, baos.toByteArray());
        return bytesToHex(sign);
    }

    static byte[] encodeHMAC(String key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        return encodeHMAC(key.getBytes(), message);
    }

    static byte[] encodeHMAC(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        return mac.doFinal(message);
    }

    static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}
