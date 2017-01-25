package com.youxibi.ddz2;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx724c4dda97632881";
    public static final String APP_SECRECT = "c566e685587ca7ba01f579e909f817c1";

    private IWXAPI api;

    private String params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, APP_ID, false);

        if (!api.isWXAppInstalled()) {
            return;
        }

        // 将该app注册到微信
        api.registerApp(APP_ID);

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wechat_sdk_demo_test";
                api.sendReq(req);
            }
        });

        findViewById(R.id.pay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetParameterTask().execute("http://www.mojing.com/2.php");

//                try {
//                    Util.httpPost("https://api.mch.weixin.qq.com/pay/unifiedorder", params);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }
        });
    }

    private class GetParameterTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Util.httpGet(urls[0]);
            } catch (IOException e) {
                return getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "result: " + result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String appid = jsonObject.optString("appid");
                String body = jsonObject.optString("body");
                String key = jsonObject.optString("key");
                String mch_id = jsonObject.optString("mch_id");
                String nonce_str = Util.getRandomString(1, 32);
//                String nonce_str = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS";
                String notify_url = jsonObject.optString("notify_url");

                /*
                Map<String, String> p = new HashMap<>();
                p.put("appid", jsonObject.optString("appid"));
                p.put("body", jsonObject.optString("body"));
                p.put("key", jsonObject.optString("key"));
                p.put("mch_id", jsonObject.optString("mch_id"));
                p.put("nonce_str", Util.getRandomString(32));
                */

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                String out_trade_no = formatter.format(new Date()) + Util.getRandomString(0, 6);
//                String out_trade_no = formatter.format(new Date());
//                String out_trade_no = "20150806125346";
//                p.put("out_trade_no", out_trade_no);

                // p.put("spbill_create_ip", jsonObject.optString("spbill_create_ip"));
                String spbill_create_ip = jsonObject.optString("spbill_create_ip");
                int total_fee = 1;
                // p.put("total_fee", "1");

                String stringSignTemp = "appid=" + appid
                        + "&body=" + body
                        + "&mch_id=" + mch_id
                        + "&nonce_str=" + nonce_str
                        + "&notify_url=" + notify_url
                        + "&out_trade_no=" + out_trade_no
                        + "&spbill_create_ip=" + spbill_create_ip
                        + "&total_fee=" + total_fee
                        + "&trade_type=APP"
                        + "&key=" + key;
                String sign = Util.MD5(stringSignTemp).toUpperCase();
                // p.put("sign", sign);

                params = "<xml>" + "<appid>" + appid + "</appid>"
                        + "<body><![CDATA[" + body + "]]></body>"
                        + "<mch_id>" + mch_id + "</mch_id>"
                        + "<nonce_str>" + nonce_str + "</nonce_str>"
                        + "<notify_url>" + notify_url + "</notify_url>"
                        + "<out_trade_no>" + out_trade_no + "</out_trade_no>"
                        + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"
                        + "<total_fee>" + total_fee + "</total_fee>"
                        + "<trade_type>APP</trade_type>"
                        + "<sign>" + sign + "</sign>"
                        + "</xml>";

                Log.i(TAG, params);

                new GetPrepayIdTask().execute("https://api.mch.weixin.qq.com/pay/unifiedorder");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetPrepayIdTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Util.httpPost(urls[0], params);
            } catch (IOException e) {
                return getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "result: " + result);

            Map<String, String> map = Util.parseXML(result);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Log.i(TAG, entry.getKey() + ": " + entry.getValue());
            }

            if (map.get("prepay_id") != null) {

            }
        }
    }
}
