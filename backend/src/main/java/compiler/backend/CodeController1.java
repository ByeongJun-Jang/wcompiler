package compiler.backend;

import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
// @RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController1 {

    private final RestTemplate restTemplate = new RestTemplate();

    // @PostMapping("/execute")
    public ResponseEntity<String> executeCode(@RequestBody CodeRequest codeRequest) {

        log.info("받은 리퀘스트 코드는 : {}, 언어는 {}, 인풋은 = {}",codeRequest.getCode(), codeRequest.getLanguage(), codeRequest.getInputValues());

        String language = codeRequest.getLanguage();
        String code = codeRequest.getCode();
        String url = getCompilerUrl(language);
        String inputValues = codeRequest.getInputValues();

        if (url == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("지원되지 않는 언어");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("inputValues", inputValues);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.info("코드 실행에 관한 문제", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("코드 실행에 관한 문제 예외 발생");
        }
    }

    private String getCompilerUrl(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "http://java-server:5002/execute";
            case "python":
                return "http://python-server:5001/execute";
            default:
                return null;
        }
    }
}
