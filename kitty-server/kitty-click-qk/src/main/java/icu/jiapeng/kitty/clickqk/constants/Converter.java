package icu.jiapeng.kitty.clickqk.constants;

import icu.jiapeng.kitty.clickqk.dto.ReportReq;
import icu.jiapeng.kitty.clickqk.entity.Operate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.UUID;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@Mapper
public interface Converter {
    Converter INSTANCE = Mappers.getMapper(Converter.class);

    Operate toOperate(ReportReq reportReq);

    @AfterMapping
    default void afterMapping(@MappingTarget Operate operate) {
        operate.setId(UUID.randomUUID().toString().replace("-", ""));
    }
}
