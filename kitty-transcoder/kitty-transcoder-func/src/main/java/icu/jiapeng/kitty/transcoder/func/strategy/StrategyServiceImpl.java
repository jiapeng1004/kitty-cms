package icu.jiapeng.kitty.transcoder.func.strategy;

import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StrategyServiceImpl implements StrategyService {

    private static final String STRATEGY_MAP_KEY = "transcode:strategy:map";

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public String createStrategy(CreateStrategyRequest request) {
        // 生成策略ID
        String strategyId = "strategy_" + UUID.randomUUID().toString().replace("-", "");

        // 创建策略对象
        StrategyVO strategyVO = new StrategyVO();
        strategyVO.setId(strategyId);
        strategyVO.setName(request.getName());
        strategyVO.setTargetFormat(request.getTargetFormat());
        strategyVO.setResolution(request.getResolution());
        strategyVO.setBitrate(request.getBitrate());
        strategyVO.setFrameRate(request.getFrameRate());
        strategyVO.setEncoder(request.getEncoder());
        strategyVO.setAddWatermark(request.getAddWatermark());
        strategyVO.setWatermarkPosition(request.getWatermarkPosition());
        strategyVO.setWatermarkPath(request.getWatermarkPath());
        strategyVO.setCreatedAt(System.currentTimeMillis());

        // 存储策略信息到 Redis
        RMap<String, StrategyVO> strategyMap = redissonClient.getMap(STRATEGY_MAP_KEY);
        strategyMap.put(strategyId, strategyVO);

        return strategyId;
    }

    @Override
    public StrategyVO getStrategy(String strategyId) {
        RMap<String, StrategyVO> strategyMap = redissonClient.getMap(STRATEGY_MAP_KEY);
        return strategyMap.get(strategyId);
    }

    @Override
    public List<StrategyVO> getStrategies() {
        RMap<String, StrategyVO> strategyMap = redissonClient.getMap(STRATEGY_MAP_KEY);
        return new ArrayList<>(strategyMap.values());
    }

    @Override
    public boolean updateStrategy(String strategyId, CreateStrategyRequest request) {
        RMap<String, StrategyVO> strategyMap = redissonClient.getMap(STRATEGY_MAP_KEY);
        StrategyVO strategyVO = strategyMap.get(strategyId);
        if (strategyVO != null) {
            strategyVO.setName(request.getName());
            strategyVO.setTargetFormat(request.getTargetFormat());
            strategyVO.setResolution(request.getResolution());
            strategyVO.setBitrate(request.getBitrate());
            strategyVO.setFrameRate(request.getFrameRate());
            strategyVO.setEncoder(request.getEncoder());
            strategyVO.setAddWatermark(request.getAddWatermark());
            strategyVO.setWatermarkPosition(request.getWatermarkPosition());
            strategyVO.setWatermarkPath(request.getWatermarkPath());
            strategyMap.put(strategyId, strategyVO);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteStrategy(String strategyId) {
        RMap<String, StrategyVO> strategyMap = redissonClient.getMap(STRATEGY_MAP_KEY);
        return strategyMap.remove(strategyId) != null;
    }
}