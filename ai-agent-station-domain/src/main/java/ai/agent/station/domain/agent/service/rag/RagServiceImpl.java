package ai.agent.station.domain.agent.service.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;

import static ai.agent.station.types.common.Constants.TAG_LIST;

@Slf4j
@Service
public class RagServiceImpl implements RagService{

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private RedissonClient redissonClient;

    @Value("${github.username}")
    private String username;

    @Value("${github.token}")
    private String token;

    @Override
    public void uploadKnowledge(String userId, String tag, List<MultipartFile> fileList) {
        log.info("上传知识库开始，用户ID：{}，知识库标签：{}", userId, tag);
        for (MultipartFile file : fileList) {
            // 创建临时文件保存上传内容
            Path tempFile = null;
            try {
                // 创建安全的临时文件
                tempFile = Files.createTempFile("knowledge_upload_", "_" + file.getOriginalFilename());
                // 将上传文件内容复制到临时文件
                file.transferTo(tempFile);
                // 使用临时文件进行文档解析
                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new PathResource(tempFile));
                List<Document> documents = tikaDocumentReader.get();
                // 切割分块
                List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
                // 添加标签
                documents.forEach(doc -> doc.getMetadata().put("knowledge", tag));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", tag));
                // 保存向量和对应文本数据
                pgVectorStore.accept(documentSplitterList);
            } catch (Exception e) {
                log.error("处理文件 {} 失败: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
            } finally {
                // 清理临时文件
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        log.warn("删除临时文件失败: {}", tempFile, e);
                    }
                }
            }
        }
        // 缓存知识库标签
        RList<String> tagList = redissonClient.getList(TAG_LIST + userId);
        if (!tagList.contains(tag)) {
            tagList.add(tag);
        }
        log.info("上传知识库完成，用户ID：{}，知识库标签：{}", userId, tag);
    }

    @Override
    public void repoGit(String userId, String tag, String repoUrl) throws IOException {
        log.info("拉取Git代码库开始，用户ID：{}，知识库标签：{}，Git代码库URL：{}", userId, tag, repoUrl);
        // 系统临时目录
        String localPath = System.getProperty("java.io.tmpdir") + "/clone-repo-" + UUID.randomUUID();
        File localDir = new File(localPath);
        try {
            // 确保目录存在
            FileUtils.forceMkdir(localDir);
            try (Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call()) {
                Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            log.info("文件路径:{}", file.toString());
                            PathResource resource = new PathResource(file);
                            TikaDocumentReader reader = new TikaDocumentReader(resource);
                            List<Document> documents = reader.get();

                            if (documents == null || documents.isEmpty()) {
                                log.warn("文件内容为空，跳过处理: {}", file);
                                return FileVisitResult.CONTINUE;
                            }

                            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
                            documents.forEach(doc -> doc.getMetadata().put("knowledge", tag));
                            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", tag));
                            pgVectorStore.accept(documentSplitterList);

                        } catch (IllegalArgumentException e) {
                            log.error("内容异常文件: {} | 错误信息: {}", file, e.getMessage());
                        } catch (Exception e) {
                            log.error("处理文件失败: {} | 错误类型: {}", file, e.getClass().getSimpleName());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 缓存知识库标签
            RList<String> tagList = redissonClient.getList(TAG_LIST + userId);
            if (!tagList.contains(tag)) {
                tagList.add(tag);
            }
        } finally {
            // 删除临时目录
            deleteDirectoryWithRetry(localDir);
        }
        log.info("拉取Git代码库完成，用户ID：{}，知识库标签：{}", userId, tag);
    }

    /**
     * 删除目录
     * @param directory
     */
    private void deleteDirectoryWithRetry(File directory) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                FileUtils.deleteDirectory(directory);
                log.info("成功删除目录: {}", directory.getAbsolutePath());
                return;
            } catch (IOException e) {
                retryCount++;
                log.warn("删除目录失败(尝试 {} / {}), 原因: {}", retryCount, 3, e.getMessage());
                if (retryCount < 3) {
                    try {
                        Thread.sleep(1000L * retryCount); // 等待时间递增
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("无法删除目录: {}", directory.getAbsolutePath(), e);
                }
            }
        }
    }

    @Override
    public List<String> getTagList(String userId) {
        return redissonClient.getList(TAG_LIST + userId);
    }

}
