package ai.agent.station.domain.agent.service.rag;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RagService {

    /**
     * 上传知识库
     * @param userId 用户ID
     * @param tag 知识库标签
     * @param fileList 文件集合
     */
    void uploadKnowledge(String userId, String tag, List<MultipartFile> fileList);

    /**
     * 拉取Git代码库并上传知识库
     * @param userId 用户ID
     * @param tag 知识库标签
     * @param repoUrl Git代码库URL
     * @return
     */
    void repoGit(String userId, String tag, String repoUrl) throws IOException;

    /**
     * 获取知识库标签集合
     * @param userId 用户ID
     * @return
     */
    List<String> getTagList(String userId);

}
