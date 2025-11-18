package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientPrompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientPromptDao {

    /**
     * 根据提示词ID获取提示词
     * @param promptId 提示词ID
     * @return
     */
    ClientPrompt getPromptByPromptId(@Param("promptId") String promptId);

}
