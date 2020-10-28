package com.aniu.course.util;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version: V1.0
 * @ClassName: SignUtils
 * @Description: 加密工具类
 * @author: hanxie
 * @create: 2020/10/28
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
public class SignUtils {

    /**
     * 请求url的所有参数拼接成字符串
     *
     * @param map
     * @return
     */
    public static String createQueryString(Map<String, String> map) {
        if (map.isEmpty()) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (null == key || "".equals(key)) {
                continue;
            }
            try {
                if (null != value && !"".equals(value)) {
                    res.append(key).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
                } else {
                    res.append(key).append("=").append(value).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (res.length() > 1) {
            return res.substring(0, res.length() - 1);
        }
        return null;
    }

    /**
     * 通过md5进行加密
     *
     * @param source 要加密的数据
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String getMd5(String source) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("md5");
        byte[] bytes = source.getBytes();
        byte[] targetBytes = digest.digest(bytes);
        char[] characters = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder builder = new StringBuilder();
        for (byte b : targetBytes) {
            int high = (b >> 4) & 15;
            int low = b & 15;
            char highChar = characters[high];
            char lowChar = characters[low];
            builder.append(highChar).append(lowChar);
        }

        return builder.toString();
    }

    /**
     * 进行MD5加密
     *
     * @param qs
     * @param time
     * @param salt
     * @return
     */
    public static String getSign(String qs, long time, String salt) {
        try {
            return getMd5(String.format("%s&time=%d&salt=%s", qs, time, salt));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将一个Map按照Key字母升序构成一个QueryString. 并且加入时间混淆的hash串
     *
     * @param queryMap query内容
     * @param time     加密时候，为当前时间；解密时，为从querystring得到的时间；
     * @param salt     加密salt
     * @return
     */
    public static String createHashedQueryString(Map<String, String> queryMap, long time, String salt) {
        Map<String, String> map = new TreeMap(queryMap);
        String qs = createQueryString(map); //生成queryString方法可自己编写
        if (qs == null) {
            return null;
        }
        time = time / 1000;
        String hash = getSign(qs, time, salt).toUpperCase();
        String thqs = String.format("%s&time=%d&hash=%s", qs, time, hash);
        return thqs;
    }

    public static void main1111(String[] args) throws IOException {
        Map<String, String> params = new HashMap<String, String>();// 需要传递的参数
        params.put("userid", "7CEFDE16F4DC35B6");
        params.put("videoid", "6AF9A296B11753689C33DC5901307461");

        String salt = "zeDnFNuXUwqE0QVzNdJ4aRH4YCevPjtt"; //秘钥
        long time = System.currentTimeMillis(); //当前时间戳
        String str = createHashedQueryString(params, time, salt);//生成http请求参数
        String url = "http://spark.bokecc.com/api/video/original?" + str;
        System.out.println(url);
        String result = httpRetrieve(url);
        System.out.println(result);
    }

    public static String httpRetrieve(String triggerURL) throws IOException {
        StringBuffer document = new StringBuffer();
        URL url = new URL(triggerURL);
        URLConnection conn = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            document.append(line);
        }
        reader.close();
        return document.toString();
    }

    public static void downloadByNIO2(String url, String saveDir, String fileName) {
        try (InputStream ins = new URL(url).openStream()) {
            Path target = Paths.get(saveDir, fileName);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String url = "http://18.cu.backup.bokecc.com/flvs/7CEFDE16F4DC35B6/2020-09-25/6AF9A296B11753689C33DC5901307461.mp4?t=1603878331&key=C599BC3CAFADE19450F63778AFFC7710";
        downloadByNIO2(url, "/vvvvv", "uuid.mp4");
    }

}
