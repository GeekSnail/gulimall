package com.example.gulimall.member.component;

import com.example.gulimall.member.vo.SocialUser;
import com.example.common.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.json.JsonParserFactory;

import java.util.Map;

public class GithubApi {
    String host = "https://api.github.com";
    String accessToken;
    Map<String, String> headers;
    public GithubApi(String token) {
        accessToken = token;
        headers = Map.of("Authorization", "Bearer " + token, "accept", "application/json");
    }
    public SocialUser user() throws Exception {
        HttpResponse response = HttpUtils.doGet(host, "/user", "get", headers, null);
        String s = EntityUtils.toString(response.getEntity());
        Map<String, Object> res = JsonParserFactory.getJsonParser().parseMap(s);
        if (response.getStatusLine().getStatusCode() == 200 && res.containsKey("id")) {
            System.out.println(s);
            SocialUser user = new SocialUser();
            user.setId(Long.parseLong(res.get("id").toString()));
            user.setUsername((String) res.get("login"));
            user.setEmail((String) res.get("email"));
            user.setAccessToken(accessToken);
            user.setType("github");
            return user;
        }
        System.err.println(s);
        return null;
    }
}
