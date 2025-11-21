package ai.agent.station.trigger.http;

import ai.agent.station.api.AgentService;
import ai.agent.station.api.dto.ChatRequestDTO;
import ai.agent.station.api.response.Response;
import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.service.execute.ExecuteService;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import ai.agent.station.domain.agent.service.rag.RagService;
import ai.agent.station.types.enums.ResponseCodeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/agent")
public class AgentController implements AgentService {

    @Resource
    private ExecuteService executeService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RagService ragService;

    @PostMapping("/chat")
    @Override
    public ResponseBodyEmitter agentChat(@RequestBody ChatRequestDTO chatRequestDTO, HttpServletResponse response) {
        try {
            // 构建流式输出对象
            ResponseBodyEmitter emitter = ResponseBodyEmitterManager.put(chatRequestDTO.getUserId(), Long.MAX_VALUE);
            // 参数校验
            if (StringUtils.isBlank(chatRequestDTO.getUserId()) || StringUtils.isBlank(chatRequestDTO.getPrompt()) ||
                    StringUtils.isBlank(chatRequestDTO.getAgentId())) {
                return ResponseBodyEmitterManager.send(chatRequestDTO.getUserId(), "参数非法");
            }
            // 异步执行Agent
            threadPoolExecutor.execute(() -> {
                try {
                    executeService.execute(
                            ChatRequestEntity.builder()
                                    .userId(chatRequestDTO.getUserId())
                                    .prompt(chatRequestDTO.getPrompt())
                                    .agentId(chatRequestDTO.getAgentId())
                                    .maxStep(chatRequestDTO.getMaxStep())
                                    .tag(chatRequestDTO.getTag())
                                    .build(),
                            emitter);
                } catch (Exception e) {
                    log.error("Agent执行异常：{}", e.getMessage(), e);
                    try {
                        emitter.send("Agent执行异常：" + e.getMessage());
                    } catch (Exception ex) {
                        log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                    }
                } finally {
                    ResponseBodyEmitterManager.remove(chatRequestDTO.getUserId());
                }
            });
            return emitter;
        } catch (Exception e) {
            log.error("Agent请求处理异常：{}", e.getMessage(), e);
            return ResponseBodyEmitterManager.send(chatRequestDTO.getUserId(), "请求处理失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload_knowledge")
    @Override
    public Response<String> uploadKnowledge(@RequestParam("userId") String userId,
                                            @RequestParam("tag") String tag,
                                            @RequestParam("fileList") List<MultipartFile> fileList) {
        try {
            // 参数校验
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(tag) ||
                    fileList == null || fileList.isEmpty()) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .data("缺少必要参数")
                        .build();
            }
            // 异步上传知识库
            threadPoolExecutor.execute(() -> ragService.uploadKnowledge(userId, tag, fileList));
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data("等待上传成功")
                    .build();
        } catch (Exception e) {
            log.error("知识库上传异常：{}", e.getMessage(), e);
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .data(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/repo_git")
    @Override
    public Response<String> repoGit(@RequestParam("userId") String userId,
                                    @RequestParam("tag") String tag,
                                    @RequestParam("repoUrl") String repoUrl) {
        try {
            // 参数校验
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(tag) || StringUtils.isBlank(repoUrl)) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .data("缺少必要参数")
                        .build();
            }
            // 异步上传知识库
            threadPoolExecutor.execute(() -> {
                try {
                    ragService.repoGit(userId, tag, repoUrl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data("等待上传成功")
                    .build();
        } catch (Exception e) {
            log.error("拉取Git代码库异常：{}", e.getMessage(), e);
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .data(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/tag_list/{userId}")
    @Override
    public Response<List<String>> getTagList(@PathVariable("userId") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Response.<List<String>>builder()
                    .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        List<String> tagList = ragService.getTagList(userId);
        return Response.<List<String>>builder()
                .code(ResponseCodeEnum.SUCCESS.getCode())
                .info(ResponseCodeEnum.SUCCESS.getInfo())
                .data(tagList)
                .build();
    }

}
