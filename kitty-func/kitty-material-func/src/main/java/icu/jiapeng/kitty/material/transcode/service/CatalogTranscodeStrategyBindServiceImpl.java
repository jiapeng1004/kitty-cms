package icu.jiapeng.kitty.material.transcode.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.material.transcode.entity.KtCatalogTranscodeStrategyBind;
import icu.jiapeng.kitty.material.transcode.mapper.KtCatalogTranscodeStrategyBindMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogTranscodeStrategyBindServiceImpl extends ServiceImpl<KtCatalogTranscodeStrategyBindMapper, KtCatalogTranscodeStrategyBind> implements CatalogTranscodeStrategyBindService {

    @Override
    public List<KtCatalogTranscodeStrategyBind> listByCatalogOrderSort(String catalogId) {
        return lambdaQuery()
                .eq(KtCatalogTranscodeStrategyBind::getCatalogId, catalogId)
                .orderByAsc(KtCatalogTranscodeStrategyBind::getSortNum)
                .list();
    }

    @Override
    public Optional<KtCatalogTranscodeStrategyBind> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public boolean save(KtCatalogTranscodeStrategyBind bind) {
        return saveOrUpdate(bind);
    }

    @Override
    public void deleteById(String id) {
        removeById(id);
    }
}
