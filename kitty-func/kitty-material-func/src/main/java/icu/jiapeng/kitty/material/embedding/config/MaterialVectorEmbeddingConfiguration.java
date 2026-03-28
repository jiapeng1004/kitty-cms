package icu.jiapeng.kitty.material.embedding.config;

import icu.jiapeng.kitty.material.embedding.KtEmbeddingPort;
import icu.jiapeng.kitty.material.embedding.adapter.NoneMaterialVectorEmbeddingAdapter;
import icu.jiapeng.kitty.material.embedding.adapter.RoutingMaterialVectorEmbeddingPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量化端口装配：默认 NONE 安全实现 + 路由入口。
 */
@Configuration
public class MaterialVectorEmbeddingConfiguration {

    @Bean
    public NoneMaterialVectorEmbeddingAdapter noneMaterialVectorEmbeddingAdapter() {
        return new NoneMaterialVectorEmbeddingAdapter();
    }

    @Bean
    public KtEmbeddingPort materialVectorEmbeddingPort(NoneMaterialVectorEmbeddingAdapter noneAdapter) {
        return new RoutingMaterialVectorEmbeddingPort(noneAdapter);
    }
}
