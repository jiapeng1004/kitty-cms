/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.common.core.util;

import java.util.Objects;

/**
 * 路径常用工具：URL/URI 或文件路径的拼接、规范化等。
 */
public final class PathUtil {


    private PathUtil() {
    }

    /**
     * 将多段路径用 "/" 拼接，自动处理首尾及中间多余斜杠。
     * 空串或 null 段会被忽略；结果不以 "/" 结尾（除非仅有一段且为 "/"）。
     *
     * @param segments 路径段，可含 null 或空串
     * @return 规范化后的路径，如 "/a/b/c"；无有效段时返回 ""
     */
    public static String builderPath(String... segments) {
        String separator = "/";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < segments.length; ++i) {
            String backPart = segments[i];
            if (i == 0) {
                stringBuilder.append(backPart);
            } else if (Objects.nonNull(backPart) && !backPart.isBlank()) {
                String frontPart = stringBuilder.toString();
                if (frontPart.endsWith(separator) && !backPart.startsWith(separator)) {
                    stringBuilder.append(backPart);
                } else if (frontPart.endsWith(separator) && backPart.startsWith(separator)) {
                    stringBuilder.append(backPart.substring(1));
                }

                if (!frontPart.endsWith(separator) && !backPart.startsWith(separator)) {
                    stringBuilder.append(separator).append(backPart);
                } else if (!frontPart.endsWith(separator) && backPart.startsWith(separator)) {
                    stringBuilder.append(backPart);
                }
            }
        }
        return stringBuilder.toString();
    }
}
