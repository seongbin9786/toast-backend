package utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class SocialConfigReader {

    /*
        1. maven install 빌드 시에는 test 경로는 war/jar 에 포함되지 않는다.

        2. 테스트 시에는 classpath 로
     */
    public static DocumentContext getSocialConfigAsJson() {
        Yaml yaml = new Yaml();
        try {
            Resource resource = new ClassPathResource("social_config.yml");
            Map<String, Object> defaultProps = yaml.load(resource.getInputStream());
            System.out.println(defaultProps);
            return JsonPath.parse(defaultProps);
        } catch (IOException e) {
            // todo: logging
        }
        return null;
    }
}
