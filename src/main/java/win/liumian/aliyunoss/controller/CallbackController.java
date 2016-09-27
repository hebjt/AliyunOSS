package win.liumian.aliyunoss.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import win.liumian.aliyunoss.util.CallbackUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by liumian on 2016/9/27.
 */
@Controller
public class CallbackController {

    @RequestMapping(value = "ossCallback",method={RequestMethod.GET})
    public void get(HttpServletRequest request, HttpServletResponse response){
        post(request,response);
    }

    @RequestMapping(value = "ossCallback",method={RequestMethod.POST})
    public void post(HttpServletRequest request, HttpServletResponse response) {

        String ossCallbackBody = null;
        boolean ret = false;
        try {
            int contentLength = Integer.parseInt(request.getHeader("content-length"));
            ossCallbackBody = CallbackUtil.getPostBody(request.getInputStream(), contentLength );
            ret = CallbackUtil.verifyOSSCallbackRequest(request, ossCallbackBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("verify result:" + ret);
        System.out.println("OSS Callback Body:" + ossCallbackBody);
        try {
            if (ret) {
                response(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK);
            } else {
                response(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 返回给Aliyun服务器
     *
     * @param request
     * @param response
     * @param results  需要返回的结果
     * @param status   处理状态
     * @throws IOException
     */
    private void response(HttpServletRequest request, HttpServletResponse response, String results, int status) throws IOException {
        String callbackFunName = request.getParameter("callback");
        response.addHeader("Content-Length", String.valueOf(results.length()));
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
            response.getWriter().println(results);
        else
            response.getWriter().println(callbackFunName + "( " + results + " )");
        response.setStatus(status);
        response.flushBuffer();
    }

}
