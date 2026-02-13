package icu.jiapeng.kitty.transcoder.func.strategy;

import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;

import java.util.List;

public interface StrategyService {

    /**
     * 创建转码策略
     * @param request 创建策略请求
     * @return 策略ID
     */
    String createStrategy(CreateStrategyRequest request);

    /**
     * 查询转码策略
     * @param strategyId 策略ID
     * @return 策略详情
     */
    StrategyVO getStrategy(String strategyId);

    /**
     * 查询所有转码策略
     * @return 策略列表
     */
    List<StrategyVO> getStrategies();

    /**
     * 更新转码策略
     * @param strategyId 策略ID
     * @param request 更新策略请求
     * @return 是否更新成功
     */
    boolean updateStrategy(String strategyId, CreateStrategyRequest request);

    /**
     * 删除转码策略
     * @param strategyId 策略ID
     * @return 是否删除成功
     */
    boolean deleteStrategy(String strategyId);
}