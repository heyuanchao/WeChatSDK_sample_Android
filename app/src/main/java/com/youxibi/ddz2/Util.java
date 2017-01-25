package com.youxibi.ddz2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class Util {

    public static String httpGet(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString, "GET", null);
            // str = readIt(stream, 500);
            str = read(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    public static String httpPost(String urlString, Map<String, String> params) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString, "POST", params);
            // str = readIt(stream, 500);
            str = read(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    private static InputStream downloadUrl(String urlString, String method, Map<String, String> params) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod(method);
//        conn.setDoInput(true);
        // Start the query
        // conn.connect();

        if (params != null) {
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(getRequestData(params, "UTF-8").getBytes());
        }

        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            return downloadUrl(conn.getHeaderField("Location"), "POST", params);
        }
        InputStream stream = conn.getInputStream();
        return stream;
    }

    /**
     * 封装请求体信息
     */
    public static String getRequestData(Map<String, String> params, String encode) {
        StringBuffer sb = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            sb.deleteCharAt(sb.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //从流中读取数据
    public static String read(InputStream inStream) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return new String(outStream.toByteArray());
    }
}
