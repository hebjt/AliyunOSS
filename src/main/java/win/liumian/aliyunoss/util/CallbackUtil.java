package win.liumian.aliyunoss.util;

import com.aliyun.oss.common.utils.BinaryUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by liumian on 2016/9/27.
 */
public class CallbackUtil {

    /**
     * 校验是不是AliyunOSS服务器发来的Callback请求
     *
     * @param request         request
     * @param ossCallbackBody request主体
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public static final boolean verifyOSSCallbackRequest(HttpServletRequest request, String ossCallbackBody)
            throws NumberFormatException, IOException {
        boolean ret = false;
        try {

            String autorizationInput = new String(request.getHeader("Authorization"));
            String pubKeyInput = request.getHeader("x-oss-pub-key-url");
            byte[] authorization = BinaryUtil.fromBase64String(autorizationInput);
            byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
            String pubKeyAddr = new String(pubKey);
            if (!pubKeyAddr.startsWith("http://gosspublic.alicdn.com/") && !pubKeyAddr.startsWith("https://gosspublic.alicdn.com/")) {
                System.out.println("pub key addr must be oss addrss");
                return false;
            }
            String retString = executeGet(pubKeyAddr);
            retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
            retString = retString.replace("-----END PUBLIC KEY-----", "");
            String queryString = request.getQueryString();
            String uri = request.getRequestURI();
            String decodeUri = java.net.URLDecoder.decode(uri, "UTF-8");
            String authStr = decodeUri;
            if (queryString != null && !queryString.equals("")) {
                authStr += "?" + queryString;
            }
            authStr += "\n" + ossCallbackBody;
            ret = doCheck(authStr, authorization, retString);
        }catch (Exception e){
            return false;
        }
        return ret;
    }




    /**
     * 检查公钥和数字签名是否正确
     *
     * @param content   需要检查的内容
     * @param sign      数字签名
     * @param publicKey 公钥
     * @return
     */
    public static final boolean doCheck(String content, byte[] sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes());
            boolean bverify = signature.verify(sign);
            return bverify;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }




    /**
     * 想AliyunOSS服务器发起请求，获得结果用于检查是否是服务器发起的Callback请求
     *
     * @param url AliyunOSS服务器路径
     * @return 请求结果
     */
    public static final String executeGet(String url) {
        BufferedReader in = null;

        String content = null;
        try {
            // 定义HttpClient
            @SuppressWarnings("resource")
            DefaultHttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            content = sb.toString();
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try {
                    in.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return content;
        }
    }


    /**
     * 获取post 主体
     *
     * @param is         request.getInputStream()
     * @param contentLen 内容长度
     * @return post内容
     */
    public static final String getPostBody(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {// Should not happen.
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return new String(message);
            } catch (IOException e) {
            }
        }
        return "";
    }



}
