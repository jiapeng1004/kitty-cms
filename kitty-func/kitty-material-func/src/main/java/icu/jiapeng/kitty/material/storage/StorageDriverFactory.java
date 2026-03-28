package icu.jiapeng.kitty.material.storage;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 存储驱动工厂（按驱动名称路由）。
 */
@Component
public class StorageDriverFactory {
    private final Map<String, StorageDriver> driverMap;

    public StorageDriverFactory(List<StorageDriver> drivers) {
        this.driverMap = drivers.stream().collect(Collectors.toMap(StorageDriver::driverName, Function.identity(), (a, b) -> a));
    }

    public StorageDriver resolve(String driverName) {
        StorageDriver driver = driverMap.get(driverName);
        if (driver == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return driver;
    }
}
