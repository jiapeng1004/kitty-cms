package icu.jiapeng.kitty.material.domain.repository;

import java.util.List;
import java.util.Optional;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
public interface BaseCrudRepository<T> {
    <S extends T> boolean save(S entity);

    <S extends T> boolean saveAll(List<S> entities);

    Optional<T> findById(String id);

    boolean existsById(String id);

    List<T> findAll();

    List<T> findAllById(List<String> ids);

    boolean deleteById(String id);

    boolean deleteAllById(List<String> ids);
}