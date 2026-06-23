package com.ypat.repository;

import com.ypat.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author dingyinxin
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    /**
     * 查询
     * @param id
     * @return
     */
    Article findById(@Param("id") Long id);
}
