package com.youxibi.ddz2.wxapi;

public class AccessToken {
    /**
     * "access_token":"gsuty9H-XjbLAlm_I1PDuZCTsicQhkO0KwBf3mx3gvTQkOGmKyDmlnMSQbNNJSz1RcCtypqzKNZXczkuJKwy_juExNwM4dZjOfskssyXbZM",
     * "expires_in":7200,
     * "refresh_token":"POYsRFj9L5HaDmDg1JBGvIBs5Q2ifenmwQpJahd4V_OPJ5opeObVIRbWGHDvEqCfyc-Pd-I7VbBv5udvw08q_R_VSr6tCuTUn8KYtrDF37g",
     * "openid":"ogveqvz3OnJdvicWmZDFXf1I8Xt4",
     * "scope":"snsapi_userinfo",
     * "unionid":"o8c-nt6tO8aIBNPoxvXOQTVJUxY0"
     */
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    @Override
    public String toString() {
        return "access_token: " + access_token + ", expires_in: " + expires_in +
                ", refresh_token: " + refresh_token + ", openid: " + openid +
                ", scope: " + scope + ", unionid: " + unionid;
    }
}
