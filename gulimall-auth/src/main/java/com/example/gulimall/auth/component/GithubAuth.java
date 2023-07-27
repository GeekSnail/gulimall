package com.example.gulimall.auth.component;

import com.example.common.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "oauth.github")
public class GithubAuth {
    String clientId;
    String clientSecret;

    public Map<String, Object> oauth(String code) throws Exception {
        Map<String, String> headers = Map.of("accept", "application/json");
        Map<String, String> body = Map.of("client_id", clientId, "client_secret", clientSecret, "code", code, "accept", "json");
        HttpResponse response = HttpUtils.doPost("https://github.com", "/login/oauth/access_token", "post", headers, null, body);
        String s = EntityUtils.toString(response.getEntity());
        Map<String, Object> resMap = JsonParserFactory.getJsonParser().parseMap(s);
        if (response.getStatusLine().getStatusCode() == 200) {
            System.out.println(s);
            //{"access_token":"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","token_type":"bearer","scope":"read:user"}
            //{"error":"xxx"}
            if (resMap.containsKey("access_token")) {
                return resMap;
            }
        }
        System.err.println(s);
        return null;
    }
}
