package com.youxibi.ddz2.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.youxibi.ddz2.MainActivity;
import com.youxibi.ddz2.R;
import com.youxibi.ddz2.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        Log.i(TAG, baseReq.toString());
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {
                    String code = ((SendAuth.Resp) resp).code;
                    String state = ((SendAuth.Resp) resp).state;

                    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + MainActivity.APP_ID + "&secret=" + MainActivity.APP_SECRECT + "&code=" + code + "&grant_type=authorization_code";
                    new GetAccessTokenTask().execute(url);
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

    private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Util.httpGet(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(WXEntryActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

                String access_token = jsonObject.optString("access_token");
                int expires_in = jsonObject.optInt("expires_in");
                String refresh_token = jsonObject.optString("refresh_token");
                String openid = jsonObject.optString("openid");
                String scope = jsonObject.optString("scope");
                String unionid = jsonObject.optString("unionid");

                String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid;
                new GetUserInfoTask().execute(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetUserInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Util.httpGet(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(WXEntryActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

                String openid = jsonObject.optString("openid");
                String nickname = jsonObject.optString("nickname");
                int sex = jsonObject.optInt("sex");
                String language = jsonObject.optString("language");
                String city = jsonObject.optString("city");
                String province = jsonObject.optString("province");
                String country = jsonObject.optString("country");
                String headimgurl = jsonObject.optString("headimgurl");
                String unionid = jsonObject.optString("unionid");

                Toast.makeText(WXEntryActivity.this, "你好，" + nickname, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
