package ai.agent.station.infrastructure.dao;

import ai.agent.station.infrastructure.dao.po.ClientAdvisor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientAdvisorDao {

    /**
     * 根据顾问ID获取Advisor
     * @param advisorId 顾问ID
     * @return
     */
    ClientAdvisor getAdvisorByAdvisorId(@Param("advisorId") String advisorId);
}
