package com.youxibi.ddz2;

import android.app.Activity;
import android.os.Bundle;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class MainActivity extends Activity {

    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx724c4dda97632881";

    private IWXAPI api;

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

        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }
}
