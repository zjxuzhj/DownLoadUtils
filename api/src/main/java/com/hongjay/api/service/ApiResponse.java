package com.hongjay.api.service;


import android.text.TextUtils;

/**
 * Api接口的Response基类
 *
 * Created by coffeexmg on 16/5/26.
 */
public class ApiResponse {

    /** 统一的返回说明 */
    public final static String ERROR_DESC = "网络错误，请重试";

    public final static String ERROR_SIGN_ERROR = ERROR_DESC+"(1000)";

    public final static String ERROR_SOCKET_TIMEOUT = ERROR_DESC+"(1001)";

    public static String createErrorDesc(String code){

        if(TextUtils.isEmpty(code)){
            return ERROR_DESC;
        }else{
            return String.format("%s(%s)",ERROR_DESC,code);
        }
    }



    /** Api接口调用成功 */
    public final static String SUCCESS = "0";

    /** Api接口签名结果,0代表签名成功,非0代表签名成功 */
    public String sign;

    /** Api接口签名描述 */
    public String sign_desc;

    /** Api 接口返回码 */
    public String res;

    /** Api 接口返回码 */
    public String resultCode;

    /** Api 接口返回描述 */
    public String message;

    public String log;




    /**
     *
     * 判断签名是否成功
     *
     * @return
     */
    public boolean isSignSuccess(){

        return SUCCESS.equals(sign);
    }

}
