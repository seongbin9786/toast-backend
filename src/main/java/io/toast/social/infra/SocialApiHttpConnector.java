package io.toast.social.infra;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/*
    Social Api Server 에 실제로 요청하는 객체

    static이 아닐 필요가 없지만, 테스트 용이성을 위해 Spring Bean으로 singleton으로 사용
 */
@Component
public class SocialApiHttpConnector {

    public String getResultJson(String url) {
        return getResultJson(false, url, null);
    }

    public String getResultJson(String url, String authorization) {
        return getResultJson(true, authorization, url);
    }

    /*
    1. Facebook의 경우

    [1] Access Token 이 이상한 경우
    HTTP 1.1 400 BAD REQUEST
    {
      "error": {
        "message": "Invalid OAuth access token.",
        "type": "OAuthException",
        "code": 190,
        "fbtrace_id": "BSb8RUC3CHv"
      }
    }

    2. Kakao의 경우

    [1] Access Token 이 이상한 경우
    HTTP 1.1 401 UNAUTHORIZED
    {
        "msg": "this access token does not exist",
        "code": -401
    }
     */
    private String getResultJson(boolean useBearer, String url, String authorization) {
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");

            if (useBearer) con.setRequestProperty("Authorization", authorization);

            boolean isBadRequest = con.getResponseCode() != HttpURLConnection.HTTP_OK;
            if (isBadRequest) throw new BadSocialAccessInfoException();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null) response.append(inputLine);
            br.close();
        } catch (SocketTimeoutException e) {
            throw new SocialApiServerNotRespondingException();
        } catch (IOException e) {
            // todo: 그냥 IOException 난 경우는 로깅해야 함
        }
        return response.toString();
    }
}
