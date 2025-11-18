package ai.agent.station.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 加载数据命令实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoadCommandEntity {

    /**
     * client客户端ID集合
     */
    private List<String> clientIdList;

}
