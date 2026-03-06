package icu.jiapeng.kitty.transcoder.func.mapping;

import icu.jiapeng.kitty.transcoder.api.TaskVO;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
    TaskVO toVO(TranscodeTask source);

    default Long toEpochMilli(LocalDateTime dt) {
        return dt == null ? null : dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
