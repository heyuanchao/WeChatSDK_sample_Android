package com.youxibi.ddz2;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
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
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, APP_ID, false);

        if (!api.isWXAppInstalled()) {
            Toast.makeText(this, "请先安装微信", Toast.LENGTH_SHORT).show();
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
            }
        });
    }

    private class GetParameterTask extends AsyncTask<String, Void, String> {

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
                Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                String appid = jsonObject.optString("appid");
                String body = jsonObject.optString("body");
                key = jsonObject.optString("key");
                String mch_id = jsonObject.optString("mch_id");
                String nonce_str = Util.getRandomString(1, 32);
//                String nonce_str = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS";
                String notify_url = jsonObject.optString("notify_url");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                String out_trade_no = formatter.format(new Date()) + Util.getRandomString(0, 6);
//                String out_trade_no = "20150806125346";

                String spbill_create_ip = jsonObject.optString("spbill_create_ip");
                int total_fee = 1;

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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(MainActivity.this, R.string.connection_error, Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, result);
            Map<String, String> map = parseXML(result);
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                Log.i(TAG, entry.getKey() + ": " + entry.getValue());
//            }

            String appid = map.get("appid");
            String partnerid = map.get("mch_id");
            String prepayid = map.get("prepay_id");
            String noncestr = Util.getRandomString(1, 32);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            PayReq req = new PayReq();
            req.appId = appid;
            req.nonceStr = noncestr;
            req.packageValue = "Sign=WXPay";
            req.partnerId = partnerid;
            req.prepayId = prepayid;
            req.timeStamp = timestamp;

            String stringSignTemp = "appid=" + appid
                    + "&noncestr=" + noncestr
                    + "&package=" + "Sign=WXPay"
                    + "&partnerid=" + partnerid
                    + "&prepayid=" + prepayid
                    + "&timestamp=" + timestamp
                    + "&key=" + key;
            req.sign = Util.MD5(stringSignTemp).toUpperCase();
            api.sendReq(req);
        }
    }

    private Map<String, String> parseXML(String xml) {
        Map<String, String> map = new HashMap<>();

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(stream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:// 开始元素事件
                        if ("appid".equals(nodeName) || "mch_id".equals(nodeName) || "prepay_id".equals(nodeName)) {
                            map.put(nodeName, parser.nextText());
                        }

                        break;
                    case XmlPullParser.END_TAG:// 结束元素事件
                        break;
                }
                eventType = parser.next();
            }
            stream.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}
