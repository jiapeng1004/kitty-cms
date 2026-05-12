## HTTP Exchange 客户端设计

### 接口定义
所有 API 接口使用 HTTP Exchange 客户端定义，其他服务可以直接引入 jar 包调用。

### 接口定义规范
- 使用 HTTP Exchange 注解（@PostMapping、@GetMapping 等）
- 使用 OpenAPI 注解（@Tag、@Operation、@Parameter 等）生成 Swagger 文档
- 使用 Validator 注解（@NotNull、@NotBlank、@Size 等）进行参数校验
- 接口定义无外部依赖，仅使用 Spring 和 Jakarta 注解

### 接口示例
```java
@Tag(name = "转码任务管理", description = "转码任务的创建、查询、取消等操作")
public interface TranscodeApi {
    
    @Operation(summary = "创建转码任务", description = "创建一个新的转码任务并返回任务ID")
    @PostMapping("/api/transcode/task")
    String createTask(@Valid @RequestBody CreateTaskRequest request);
    
    @Operation(summary = "查询转码任务", description = "根据任务ID查询转码任务详情")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @GetMapping("/api/transcode/task/{id}")
    TaskVO getTask(@PathVariable String id);
    
    @Operation(summary = "取消转码任务", description = "取消指定的转码任务")
    @Parameter(name = "id", description = "任务ID", required = true, example = "task_123456")
    @DeleteMapping("/api/transcode/task/{id}")
    Boolean cancelTask(@PathVariable String id);
}
```

### 请求对象示例
```java
public class CreateTaskRequest {
    
    @NotBlank(message = "输入文件不能为空")
    @Size(max = 500, message = "输入文件路径长度不能超过500")
    @Schema(description = "输入文件路径", example = "/path/to/video.mp4")
    private String inputFile;
    
    @NotBlank(message = "策略ID不能为空")
    @Size(max = 100, message = "策略ID长度不能超过100")
    @Schema(description = "转码策略ID", example = "strategy_001")
    private String strategyId;
    
    @Valid
    @Schema(description = "通知配置")
    private List<NotificationConfig> notifications;
    
    @Schema(description = "任务优先级", example = "1")
    private Integer priority;
}
```

### 通知配置对象示例
```java
public class NotificationConfig {
    
    @NotBlank(message = "通知方式不能为空")
    @Schema(description = "通知方式（MQ、HTTP、WebSocket）", example = "MQ")
    private String method;
    
    @NotBlank(message = "通知目标不能为空")
    @Size(max = 500, message = "通知目标长度不能超过500")
    @Schema(description = "通知目标（队列名、URL等）", example = "transcode.notifications")
    private String target;
}
```

### 响应对象示例
```java
@Schema(description = "转码任务详情")
public class TaskVO {
    
    @Schema(description = "任务ID", example = "task_123456")
    private String id;
    
    @Schema(description = "任务状态", example = "PROCESSING")
    private String status;
    
    @Schema(description = "转码进度", example = "50")
    private Integer progress;
    
    @Schema(description = "输入文件路径", example = "/path/to/video.mp4")
    private String inputFile;
    
    @Schema(description = "输出文件路径", example = "/path/to/output.mp4")
    private String outputFile;
    
    @Schema(description = "策略ID", example = "strategy_001")
    private String strategyId;
    
    @Schema(description = "创建时间", example = "1739457045000")
    private Long createdAt;
    
    @Schema(description = "开始时间", example = "1739457050000")
    private Long startedAt;
    
    @Schema(description = "完成时间", example = "1739457345000")
    private Long completedAt;
    
    @Schema(description = "错误信息", example = "转码失败：FFmpeg 执行错误")
    private String errorMessage;
}
```

### 客户端配置
```java
@Configuration
public class TranscodeClientConfig {
    
    @Bean
    public TranscodeApi transcodeApi() {
        return HttpServiceProxyFactory.builder()
            .baseUrl("http://localhost:8080")
            .build()
            .createClient(TranscodeApi.class);
    }
}
```

### 认证拦截器
```java
@Component
public class AuthInterceptor implements ClientExchangeFilterFunction {
    
    private final AccessKeyProvider accessKeyProvider;
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String accessKey = accessKeyProvider.getAccessKey();
        String secretKey = accessKeyProvider.getSecretKey();
        
        String signature = generateSignature(request, secretKey);
        
        ClientRequest authRequest = ClientRequest.from(request)
            .header("X-Client-Id", accessKey)
            .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
            .header("X-Signature", signature)
            .build();
        
        return next.exchange(authRequest);
    }
}
```

### Swagger 配置
```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("转码服务 API")
                .version("1.0.0")
                .description("转码服务 RESTful API 文档"))
            .addSecurityItem(new SecurityRequirement().addList("apiKey"))
            .components(new Components()
                .addSecuritySchemes("apiKey", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Signature")));
    }
}
```

### Validator 配置
```java
@Configuration
public class ValidatorConfig {
    
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
```

### 注解说明

#### OpenAPI 注解
- **@Tag**：接口分组，用于 Swagger 文档分类
- **@Operation**：接口描述，包含摘要和详细说明
- **@Parameter**：参数描述，包含名称、描述、是否必填、示例值等
- **@Schema**：对象属性描述，用于生成 Swagger 文档
- **@ApiResponse**：响应描述，包含响应码、描述、响应类型等

#### Validator 注解
- **@NotNull**：字段不能为 null
- **@NotBlank**：字符串不能为空或空白
- **@NotEmpty**：集合不能为空
- **@Size**：字符串或集合长度限制
- **@Min**：数值最小值
- **@Max**：数值最大值
- **@Pattern**：正则表达式验证
- **@Email**：邮箱格式验证
- **@Valid**：级联验证，用于嵌套对象

### Swagger 文档访问
- **开发环境**：http://localhost:8080/swagger-ui.html
- **生产环境**：根据配置禁用或启用

### 无外部依赖
- 仅使用 Spring Boot 自带的 springdoc-openapi-starter
- 仅使用 Jakarta Validation 注解
- 不依赖其他第三方库