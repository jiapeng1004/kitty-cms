package icu.jiapeng.kitty.transcoder.func.mapping;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * MapStruct 映射：TranscodeTask -> TaskVO。
 * 时间字段 LocalDateTime 转为毫秒时间戳 Long。
 */
@Mapper(componentModel = "spring")
public interface TaskVoMapper {

    @Mapping(target = "taskType", source = "taskType")
    @Mapping(target = "createdAt", expression = "java(toEpochMilli(source.getCreatedAt()))")
    @Mapping(target = "startedAt", expression = "java(toEpochMilli(source.getStartedAt()))")
    @Mapping(target = "completedAt", expression = "java(toEpochMilli(source.getCompletedAt()))")
    @Mapping(target = "inputFile", source = "inputPath")
    @Mapping(target = "outputFile", source = "outputPath")
    @Mapping(target = "notifications", expression = "java(parseNotifications(source.getNotificationConfig()))")
    TaskVO toVO(TranscodeTask source);

    default Long toEpochMilli(LocalDateTime dt) {
        return dt == null ? null : dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    default List<NotificationConfig> parseNotifications(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            List<NotificationConfig> list = JSON.parseArray(json, NotificationConfig.class);
            return list == null || list.isEmpty() ? null : list;
        } catch (Exception e) {
            return null;
        }
    }
}
