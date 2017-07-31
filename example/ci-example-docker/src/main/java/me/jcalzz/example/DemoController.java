package me.jcalzz.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cj on 2017/7/28.
 */
@RestController
public class DemoController {


    @Value("${server.port:unknown}" )
    String serverPort;

    private static String getRemoteIp(HttpServletRequest request,String defalutVal){

        final String[] IP_HEADER_CANDIDATES = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR" };
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        String remoteIp = request.getRemoteAddr();
        return (remoteIp==null || remoteIp.isEmpty())?defalutVal:remoteIp;
    }
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    HttpEntity<?> home(HttpServletRequest request){
        String clientAddress = getRemoteIp(request,"unknown");
        String hostName="unknown";
        try {
            hostName= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Map<String,String> messages = new HashMap<>();
        messages.put("server.port",serverPort);
        messages.put("server.hostName",hostName);
        messages.put("youAddress",clientAddress);
        return ResponseEntity.ok(messages);
    }
}
