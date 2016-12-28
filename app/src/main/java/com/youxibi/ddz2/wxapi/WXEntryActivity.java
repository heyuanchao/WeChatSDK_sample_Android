package com.youxibi.ddz2.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.youxibi.ddz2.MainActivity;
import com.youxibi.ddz2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXEntryActivity";
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, MainActivity.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq baseReq) {
        Log.i("@@@", baseReq.toString());
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {
                    String code = ((SendAuth.Resp) resp).code;
                    String state = ((SendAuth.Resp) resp).state;
                    getAccessToken(code, state);
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                break;
            default:
                break;
        }

        finish();
    }

    private void getAccessToken(String code, String state) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + MainActivity.APP_ID + "&secret=" + MainActivity.APP_SECRECT + "&code=" + code + "&grant_type=authorization_code";
        new GetAccessTokenTask().execute(url);
    }

    private void getUserInfo(AccessToken token) {
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + token.getAccess_token() + "&openid=" + token.getOpenid();
        new GetUserInfoTask().execute(url);
    }

    private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                return getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                AccessToken token = new AccessToken();
                token.setAccess_token(jsonObject.optString("access_token"));
                token.setExpires_in(jsonObject.optInt("expires_in"));
                token.setRefresh_token(jsonObject.optString("refresh_token"));
                token.setOpenid(jsonObject.optString("openid"));
                token.setScope(jsonObject.optString("scope"));
                token.setUnionid(jsonObject.optString("unionid"));

                Log.i(TAG, token.toString());

                getUserInfo(token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetUserInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                return getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                WxUserInfo userinfo = new WxUserInfo();
                userinfo.setOpenid(jsonObject.optString("openid"));
                userinfo.setNickname(jsonObject.optString("nickname"));
                userinfo.setSex(jsonObject.optInt("sex"));
                userinfo.setLanguage(jsonObject.optString("language"));
                userinfo.setCity(jsonObject.optString("city"));
                userinfo.setProvince(jsonObject.optString("province"));
                userinfo.setCountry(jsonObject.optString("country"));
                userinfo.setHeadimgurl(jsonObject.optString("headimgurl"));
                userinfo.setUnionid(jsonObject.optString("unionid"));

                Log.i(TAG, userinfo.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            // str = readIt(stream, 500);
            str = read(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        // conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            return downloadUrl(conn.getHeaderField("Location"));
        }
        InputStream stream = conn.getInputStream();
        return stream;
    }

    private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    //从流中读取数据
    private String read(InputStream inStream) throws IOException{
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
