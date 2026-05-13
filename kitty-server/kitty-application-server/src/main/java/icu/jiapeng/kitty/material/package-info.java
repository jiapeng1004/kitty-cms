/**
 * kitty-material：按业务域分包，域内四层 — {@code controller} / {@code service} / {@code mapper} / {@code entity}
 * （PO、仓储实现、MapStruct convert 等放在 {@code entity}；横切能力在 {@code support}）。
 * <p>
 * 检索（ES）适配在 {@code search.adapter}；索引/字段名等约定见 {@link icu.jiapeng.kitty.material.search.MaterialSearchConstants}；全局异常在 {@code support.web}。
 * {@code domain} 仅保留领域模型与端口接口；需要 Redis/Kafka 等基础设施的领域实现放在对应业务域 {@code service} 下。
 */
@ApplicationModule(allowedDependencies = {"material.api", "user.api", "common", "transcoder"})
package icu.jiapeng.kitty.material;

import org.springframework.modulith.ApplicationModule;