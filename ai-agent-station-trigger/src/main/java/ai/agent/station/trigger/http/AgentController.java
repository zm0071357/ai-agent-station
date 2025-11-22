package ai.agent.station.trigger.http;

import ai.agent.station.api.AgentService;
import ai.agent.station.api.dto.ChatRequestDTO;
import ai.agent.station.api.response.Response;
import ai.agent.station.domain.agent.model.entity.ChatRequestEntity;
import ai.agent.station.domain.agent.service.execute.ExecuteService;
import ai.agent.station.domain.agent.service.execute.manager.ResponseBodyEmitterManager;
import ai.agent.station.domain.agent.service.rag.RagService;
import ai.agent.station.types.enums.ResponseCodeEnum;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
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
@SaCheckLogin
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
            // 设置SSE响应头
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            // 参数校验
            if (StringUtils.isBlank(chatRequestDTO.getPrompt()) || StringUtils.isBlank(chatRequestDTO.getAgentId()) ||
                StringUtils.isBlank(chatRequestDTO.getSessionId())) {
                throw new RuntimeException(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo());
            }

            // 构建流式输出对象
            String key = StpUtil.getLoginIdAsString() + ":" + chatRequestDTO.getSessionId();
            ResponseBodyEmitter emitter = ResponseBodyEmitterManager.put(key, Long.MAX_VALUE);

            // 异步执行Agent
            threadPoolExecutor.execute(() -> {
                try {
                    executeService.execute(
                            ChatRequestEntity.builder()
                                    .key(key)
                                    .userId(StpUtil.getLoginIdAsString())
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
                    ResponseBodyEmitterManager.remove(key);
                }
            });
            return emitter;
        } catch (SaTokenException e) {
            log.error("登录超时");
            ResponseBodyEmitter errorEmitter = new ResponseBodyEmitter();
            try {
                errorEmitter.send("登录超时。请重新登录");
                errorEmitter.complete();
            } catch (Exception ex) {
                log.error("发送登录超时信息失败：{}", ex.getMessage(), ex);
            }
            return errorEmitter;
        } catch (Exception e) {
            log.error("Agent请求处理异常：{}", e.getMessage(), e);
            ResponseBodyEmitter errorEmitter = new ResponseBodyEmitter();
            try {
                errorEmitter.send("请求处理异常：" + e.getMessage());
                errorEmitter.complete();
            } catch (Exception ex) {
                log.error("发送错误信息失败：{}", ex.getMessage(), ex);
            }
            return errorEmitter;
        }
    }

    @PostMapping("/upload_knowledge")
    @Override
    public Response<String> uploadKnowledge(@RequestParam("tag") String tag,
                                            @RequestParam("fileList") List<MultipartFile> fileList) {
        try {
            // 参数校验
            if (StringUtils.isBlank(tag) || fileList == null || fileList.isEmpty()) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 异步上传知识库
            threadPoolExecutor.execute(() -> ragService.uploadKnowledge(StpUtil.getLoginIdAsString(), tag, fileList));
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data("等待上传成功")
                    .build();
        } catch (SaTokenException e) {
            return Response.<String>builder()
                    .code(ResponseCodeEnum.LOGIN_TIMEOUT.getCode())
                    .info(ResponseCodeEnum.LOGIN_TIMEOUT.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("知识库上传异常：{}", e.getMessage(), e);
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("/repo_git")
    @Override
    public Response<String> repoGit(@RequestParam("tag") String tag,
                                    @RequestParam("repoUrl") String repoUrl) {
        try {
            // 参数校验
            if (StringUtils.isBlank(tag) || StringUtils.isBlank(repoUrl)) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .data("缺少必要参数")
                        .build();
            }
            // 异步上传知识库
            threadPoolExecutor.execute(() -> {
                try {
                    ragService.repoGit(StpUtil.getLoginIdAsString(), tag, repoUrl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data("等待上传成功")
                    .build();
        } catch (SaTokenException e) {
            return Response.<String>builder()
                    .code(ResponseCodeEnum.LOGIN_TIMEOUT.getCode())
                    .info(ResponseCodeEnum.LOGIN_TIMEOUT.getInfo())
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

    @PostMapping("/tag_list")
    @Override
    public Response<List<String>> getTagList() {
        try {
            List<String> tagList = ragService.getTagList(StpUtil.getLoginIdAsString());
            return Response.<List<String>>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data(tagList)
                    .build();
        } catch (SaTokenException e) {
            return Response.<List<String>>builder()
                    .code(ResponseCodeEnum.LOGIN_TIMEOUT.getCode())
                    .info(ResponseCodeEnum.LOGIN_TIMEOUT.getInfo())
                    .build();
        } catch (Exception e) {
            return Response.<List<String>>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .build();
        }
    }

}
