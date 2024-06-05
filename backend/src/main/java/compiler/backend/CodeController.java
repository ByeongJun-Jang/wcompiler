package compiler.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController {

    private Process runProcess;

    @PostMapping("/compile")
    public ResponseEntity<String> compileCode(@RequestBody CodeRequest codeRequest) {
        log.info("받은 값: code={}, language={}", codeRequest.getCode(), codeRequest.getLanguage());

        String language = codeRequest.getLanguage().toLowerCase();
        String code = codeRequest.getCode();

        try {
            switch (language) {
                case "java":
                    return compileJavaCode(code);
                case "python":
                    return compilePythonCode(code);
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("test");
            }
        } catch (Exception e) {
            log.error("Compilation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception");
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeCode(@RequestBody CodeRequest codeRequest) {
        log.info("받은 입력 값: {}", codeRequest.getInputValues());

        String inputValues = codeRequest.getInputValues();

        try {
            if (runProcess != null) {
                runProcess.getOutputStream().write((inputValues + "\n").getBytes());
                runProcess.getOutputStream().flush();

                String output = new String(runProcess.getInputStream().readAllBytes());
                return ResponseEntity.ok(output);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("실행 불가 값");
            }
        } catch (Exception e) {
            log.error("Execution error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception");
        }
    }

    private ResponseEntity<String> compileJavaCode(String code) throws Exception {
        Files.write(Paths.get("UserCode.java"), code.getBytes());

        ProcessBuilder compileBuilder = new ProcessBuilder("javac", "UserCode.java");
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() != 0) {
            String error = new String(compileProcess.getErrorStream().readAllBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        runProcess = new ProcessBuilder("java", "UserCode").start();
        return ResponseEntity.ok("Java 컴파일 성공");
    }

    private ResponseEntity<String> compilePythonCode(String code) throws Exception {
        Files.write(Paths.get("UserCode.py"), code.getBytes());

        runProcess = new ProcessBuilder("python3", "UserCode.py").start();
        return ResponseEntity.ok("Python 코드 준비 완료");
    }

    private ResponseEntity<String> compilePythonCode1(String code) throws Exception {
        Files.write(Paths.get("UserCode.py"), code.getBytes());

        ProcessBuilder compileBuilder = new ProcessBuilder("python3", "UserCode.py");
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() != 0){
            String error = new String(compileProcess.getErrorStream().readAllBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        runProcess = new ProcessBuilder("python3", "UserCode.py").start();
        return ResponseEntity.ok("Python 컴파일 성공");
    }
}
