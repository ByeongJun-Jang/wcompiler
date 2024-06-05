package compiler.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@RestController
// @RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController2 {

    @PostMapping("/execute")
    public ResponseEntity<String> executeCode(@RequestBody CodeRequest codeRequest) {
        log.info("받은 request: code={}, 언어={}, 입력값={}",
                codeRequest.getCode(), codeRequest.getLanguage(), codeRequest.getInputValues());

        String language = codeRequest.getLanguage().toLowerCase();
        String code = codeRequest.getCode();
        String inputValues = codeRequest.getInputValues();

        try {
            switch (language) {
                case "java":
                    return executeJavaCode(code, inputValues);
                case "python":
                    return executePythonCode(code, inputValues);
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("테스트");
            }
        } catch (Exception e) {
            log.error("실행못하는 코드 (백)", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception occurred 스프링");
        }
    }

    private ResponseEntity<String> executeJavaCode(String code, String inputValues) throws Exception {
        Files.write(Paths.get("UserCode.java"), code.getBytes());

        ProcessBuilder compileBuilder = new ProcessBuilder("javac", "UserCode.java");
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() != 0) {
            String error = new String(compileProcess.getErrorStream().readAllBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        ProcessBuilder runBuilder = new ProcessBuilder("java", "UserCode");

        if (inputValues != null && !inputValues.isEmpty()) {
            Files.write(Paths.get("input.txt"), inputValues.getBytes());
            runBuilder.redirectInput(new File("input.txt"));
        }

        Process runProcess = runBuilder.start();
        String output = new String(runProcess.getInputStream().readAllBytes());
        runProcess.waitFor();

        if (runProcess.exitValue() != 0) {
            String error = new String(runProcess.getErrorStream().readAllBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok(output);
    }

    private ResponseEntity<String> executePythonCode(String code, String inputValues) throws Exception {
        Files.write(Paths.get("UserCode.py"), code.getBytes());

        ProcessBuilder runBuilder = new ProcessBuilder("python3", "UserCode.py");

        if (inputValues != null && !inputValues.isEmpty()) {
            Files.write(Paths.get("input.txt"), inputValues.getBytes());
            runBuilder.redirectInput(new File("input.txt"));
        }

        Process runProcess = runBuilder.start();
        String output = new String(runProcess.getInputStream().readAllBytes());
        runProcess.waitFor();

        if (runProcess.exitValue() != 0) {
            String error = new String(runProcess.getErrorStream().readAllBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok(output);
    }
}
