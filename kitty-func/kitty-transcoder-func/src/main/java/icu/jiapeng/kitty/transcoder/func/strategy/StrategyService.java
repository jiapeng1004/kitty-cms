package icu.jiapeng.kitty.transcoder.func.strategy;

import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;

import java.util.List;

public interface StrategyService {

    /**
     * 创建转码策略
     * @param request 创建策略请求
     * @return 策略ID（root_id）
     */
    Long createStrategy(CreateStrategyRequest request);

    /**
     * 查询转码策略
     * @param strategyId 策略ID（root_id）
     * @return 策略详情
     */
    StrategyVO getStrategy(Long strategyId);

    /**
     * 查询所有转码策略
     * @return 策略列表
     */
    List<StrategyVO> getStrategies();

    /**
     * 更新转码策略
     * @param strategyId 策略ID（root_id）
     * @param request 更新策略请求
     * @return 是否更新成功
     */
    boolean updateStrategy(Long strategyId, CreateStrategyRequest request);

    /**
     * 删除转码策略
     * @param strategyId 策略ID（root_id）
     * @return 是否删除成功
     */
    boolean deleteStrategy(Long strategyId);

    /**
     * 修改策略ID。若新ID已被占用则抛出 IllegalArgumentException。
     * @param oldId 当前策略ID
     * @param newId 新策略ID
     */
    void updateStrategyId(Long oldId, Long newId);
}