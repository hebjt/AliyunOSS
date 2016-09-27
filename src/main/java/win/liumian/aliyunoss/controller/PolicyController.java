package win.liumian.aliyunoss.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import win.liumian.aliyunoss.policy.PostObjectPolicy;

/**
 * Created by liumian on 2016/9/27.
 */

@Controller
public class PolicyController {

    @RequestMapping(value = "createPolicy")
    @ResponseBody
    public String createPolicy(){

        return PostObjectPolicy.createPolicy("test").toJSONString();

    }

}
