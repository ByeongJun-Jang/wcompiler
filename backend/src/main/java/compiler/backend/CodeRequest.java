package compiler.backend;

import lombok.Data;

@Data
public class CodeRequest {
    private String code;
    private String language;
    private String inputValues;
}
