package icu.jiapeng.kitty.material.transcode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.material.transcode.entity.KtCatalogTranscodeStrategyBind;

import java.util.List;
import java.util.Optional;

public interface CatalogTranscodeStrategyBindService extends IService<KtCatalogTranscodeStrategyBind> {

    List<KtCatalogTranscodeStrategyBind> listByCatalogOrderSort(String catalogId);

    Optional<KtCatalogTranscodeStrategyBind> findById(String id);

    boolean save(KtCatalogTranscodeStrategyBind bind);

    void deleteById(String id);
}
