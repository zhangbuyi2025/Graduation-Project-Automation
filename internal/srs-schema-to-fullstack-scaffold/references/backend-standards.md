# 后端编码规范（Spring Boot 3 + MyBatis-Plus）

本文件是 `srs-schema-to-fullstack-scaffold` 阶段 ① 的强制编码标准。
技术栈基线：Spring Boot 3.1.x、Java 17、MyBatis-Plus 3.5.x（spring-boot3-starter）、Spring Security、jjwt、Lombok、MySQL Connector/J。

## 分层与命名（保留现有框架）

- 基础包名以现有工程为准（**含历史拼写也不改**）
- 分层职责：
  - `common/`：统一返回 `Result`、状态/类型常量、枚举
  - `config/`：安全配置、MyBatis 配置、字段自动填充处理器
  - `controller/`：`@RestController`，只做参数接收与结果返回
  - `dto/`：请求参数（`XxxDTO`、`XxxRequest`）
  - `vo/`：响应视图（`XxxVO`）
  - `entity/`：数据库实体（`@TableName`）
  - `mapper/`：`extends BaseMapper<Entity>`
  - `service/`：业务接口 `IXxxService`
  - `service/impl/`：`XxxServiceImpl`
  - `service/infrastructure/`：文件存储、Excel 导出等基础设施
  - `exception/`：业务异常 + 全局异常处理
  - `filter/`：JWT 认证过滤器
  - `utils/`：`SecurityUtils` 等

## 统一返回 Result

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> Result<T> success(T data) { /* code=200 */ }
    public static <T> Result<T> success() { return success(null); }
    public static <T> Result<T> error(int code, String message) { /* ... */ }
}
```

## 状态 / 类型常量

不使用散落魔法字符串，集中在 `common/`：
```java
public final class ArchiveStatus {
    public static final String PENDING = "PENDING";
    public static final String FIRST_REVIEW = "FIRST_REVIEW";
    public static final String MIDDLE_REVIEW = "MIDDLE_REVIEW";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String REJECTED = "REJECTED";
    private ArchiveStatus() {}
}
```
取值以 SRS 状态机为准，**不沿用 SQL 视图笔误**。

## 实体类

```java
@Data
@TableName("t_archive")
public class Archive {
    @TableId(value = "archive_id", type = IdType.AUTO)
    private Long archiveId;

    @TableField("instance_id")
    private Long instanceId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
```

### 自动填充处理器

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

## Mapper

```java
@Mapper
public interface ArchiveMapper extends BaseMapper<Archive> {

    @Select("<script>"
          + "SELECT a.*, c.course_name, c.course_code, u.real_name AS teacher_name "
          + "FROM t_archive a "
          + "LEFT JOIN t_course_instance ci ON a.instance_id = ci.instance_id "
          + "LEFT JOIN t_course c ON ci.course_id = c.course_id "
          + "LEFT JOIN t_user u ON ci.teacher_id = u.user_id "
          + "<where>"
          + "  <if test='status != null'> AND a.status = #{status} </if>"
          + "  <if test='collegeId != null'> AND ci.college_id = #{collegeId} </if>"
          + "  <if test='deptId != null'> AND ci.dept_id = #{deptId} </if>"
          + "</where>"
          + " ORDER BY a.created_at DESC"
          + "</script>")
    List<ArchiveListVO> selectListWithRelations(@Param("status") String status,
                                                @Param("collegeId") Long collegeId,
                                                @Param("deptId") Long deptId);
}
```
- 配置开启下划线转驼峰（`mybatis-plus.configuration.map-underscore-to-camel-case=true`）
- 简单 CRUD 用 `BaseMapper` 方法，不手写 SQL；多表关联才用 `@Select`

## Service

```java
public interface IArchiveService extends IService<Archive> {
    void submit(ArchiveSubmitRequest request);
    void firstReview(ReviewDTO dto, User reviewer);
    void middleReview(ReviewDTO dto, User reviewer);
    void secondReview(ReviewDTO dto, User reviewer);
    List<ArchiveListVO> listArchived(Long collegeId, Long deptId, Long semesterId,
                                     String status, String archiveType, String keyword);
}

@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl extends ServiceImpl<ArchiveMapper, Archive>
        implements IArchiveService {
    private final FileStorageService fileStorageService;
    // ...
}
```

## 文件存储服务（三重校验）

```java
public StoredFile store(MultipartFile file) {
    String original = file.getOriginalFilename();
    // 1. 扩展名
    if (original == null || !original.toLowerCase().endsWith(".pdf")) {
        throw new BusinessException("仅支持 PDF");
    }
    // 2. 大小
    if (file.getSize() > 50L * 1024 * 1024) {
        throw new BusinessException("单个附件不能超过 50MB");
    }
    // 3. 文件头魔数
    byte[] head = new byte[5];
    try (InputStream in = file.getInputStream()) {
        in.read(head);
    }
    if (!"%PDF-".equals(new String(head, StandardCharsets.US_ASCII))) {
        throw new BusinessException("文件内容不是有效的 PDF");
    }
    // SHA-256 + UUID 落盘
    String sha256 = DigestUtils.sha256Hex(file.getInputStream());
    String storedName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
    // ... 保存到 file.upload-dir，返回 StoredFile
}
```

## Excel 导出服务（POI）

- 引入 `poi`、`poi-ooxml` 5.2.x
- 表头加粗居中，`autoSizeColumn` 自适应列宽
- 文件名中文需 URL 编码：
  ```java
  String name = URLEncoder.encode("教学档案归档清单.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
  response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + name);
  ```
- 用 try-with-resources 管理 `XSSFWorkbook`

## 异常处理

```java
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(String message) { this(400, message); }
    public BusinessException(int code, String message) { super(message); this.code = code; }
}

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    // MethodArgumentNotValidException、AccessDeniedException 等兜底
}
```

## 安全与权限

- JWT 过滤器角色映射：`"ROLE_" + role.toUpperCase()`（`dean_chair → ROLE_DEAN_CHAIR`）
- 安全配置：登录即可访问的路径放行；方法级权限交给 `@PreAuthorize`
  ```java
  @PostMapping("/review/first")
  @PreAuthorize("hasRole('DEAN_CHAIR')")
  public Result<Void> first(@Valid @RequestBody ReviewDTO dto) { ... }
  ```
- 开启方法级安全 `@EnableMethodSecurity`

## 依赖补齐

- 参数校验：`spring-boot-starter-validation`（Boot 3 不随 web 自带）
- Excel：`poi`、`poi-ooxml`
- 其余（security、jwt、redis、mysql、lombok）以现有 `pom.xml` 为准

## 禁止事项

- 不写散落魔法字符串（用常量 / 枚举）
- 不手写单表 CRUD（用 `BaseMapper` / `IService`）
- 不省略文件内容校验（仅校验扩展名可被绕过）
- 不在 Controller 写业务逻辑
- 不改包名、分层目录
