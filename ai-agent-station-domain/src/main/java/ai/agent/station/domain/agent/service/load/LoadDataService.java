package ai.agent.station.domain.agent.service.load;

import java.util.List;
import java.util.Map;

public interface LoadDataService {

    /**
     * 加载数据
     * @param clientIdList 客户端ID集合
     * @param dataMap 数据聚合Map
     */
    void loadData(List<String> clientIdList, Map<String, Object> dataMap);
}
