package ai.agent.station.api;

import ai.agent.station.api.dto.ChatRequestDTO;
import ai.agent.station.api.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

public interface AgentService {

    /**
     * 流式对话
     * @param chatRequestDTO
     * @param response
     * @return
     */
    ResponseBodyEmitter agentChat(ChatRequestDTO chatRequestDTO, HttpServletResponse response);

    /**
     * 上传知识库
     * @param tag 知识库标签
     * @param fileList 文件集合
     * @return
     */
    Response<String> uploadKnowledge(String tag, List<MultipartFile> fileList);

    /**
     * 拉取Git代码库并上传知识库
     * @param tag 知识库标签
     * @param repoUrl Git代码库URL
     * @return
     */
    Response<String> repoGit(String tag, String repoUrl);

    /**
     * 获取知识库标签集合
     * @return
     */
    Response<List<String>> getTagList();

}
