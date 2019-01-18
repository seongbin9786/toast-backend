package utils

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.yaml.snakeyaml.Yaml

class SocialConfigReader {

    static DocumentContext getSocialConfigAsJson() {
        Yaml yaml = new Yaml()
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("social_config.yml")
        Map<String, Object> defaultProps = (Map<String, Object>) yaml.loadAll(inputStream).iterator().next() // Spring default 문서 영역
        System.out.println(defaultProps)
        return JsonPath.parse(defaultProps)
    }
}
