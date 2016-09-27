package win.liumian.aliyunoss.policy;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.apache.log4j.Logger;
import win.liumian.aliyunoss.util.Constant;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by liumian on 2016/9/27.
 */
public class PostObjectPolicy {

    private static Logger logger = Logger.getLogger(PostObjectPolicy.class);

    /**
     * 将生成的object直接返回给前端
     * @return
     */
    public static JSONObject createPolicy(String dir) {
        OSSClient client = new OSSClient(Constant.ENDPOINT, Constant.ACCESS_ID, Constant.ACCESS_KEY);
        JSONObject policyObject = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<String, Object>();
            respMap.put("accessid", Constant.ACCESS_ID);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", Constant.HOST);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            policyObject = new JSONObject(respMap);
            System.out.println(policyObject.toString());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return policyObject;
    }

}
