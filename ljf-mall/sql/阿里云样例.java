//package com.ljf;
//
//public class Test {
//    public static void main(String[] args) {
//        //实例化客户端
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
//                APP_ID,
//                APP_PRIVATE_KEY,
//                "json",
//                CHARSET,
//                ALIPAY_PUBLIC_KEY,
//                "RSA2");
//    //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.open.public.template.message.industry.modify
//        AlipayOpenPublicTemplateMessageIndustryModifyRequest request = new AlipayOpenPublicTemplateMessageIndustryModifyRequest();
//
////SDK已经封装掉了公共参数，这里只需要传入业务参数
////此次只是参数展示，未进行字符串转义，实际情况下请转义
//        request.setBizContent("  {" +
//                "    \"primary_industry_name\":\"IT科技/IT软件与服务\"," +
//                "    \"primary_industry_code\":\"10001/20102\"," +
//                "    \"secondary_industry_code\":\"10001/20102\"," +
//                "    \"secondary_industry_name\":\"IT科技/IT软件与服务\"" +
//                " }");
//
//        AlipayOpenPublicTemplateMessageIndustryModifyResponse response = alipayClient.execute(request);
////调用成功，则处理业务逻辑
//        if(response.isSuccess()){
//            //.....
//        }
//    }
//}
