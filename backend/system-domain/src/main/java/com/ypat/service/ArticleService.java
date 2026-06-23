package com.ypat.service;

import com.ypat.ArticleQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.Article;
import com.ypat.repository.ArticleRepository;
import com.ypat.util.CommonUtils;
import com.ypat.util.CopyUtil;
import com.ypat.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public void save(ArticleQo articleQo){
        if(articleQo.getId()==null){
            Article article = CopyUtil.copy(articleQo, Article.class);
            article.setPlat("00");
            article.setReadtimes(0);
            article.setCredate(new Date());
            articleRepository.save(article);
        }else{
            Article old = get(articleQo.getId());
            if(old==null){
                throw new SysException(ResponseCode.FAIL_NOT);
            }
            CopyUtil.copyIgnoreNull(articleQo,old);
            articleRepository.save(old);
        }
    }

    public void upDown(ArticleQo articleQo){
        Article article = get(articleQo.getId());
        article.setStatus(articleQo.getStatus());
        articleRepository.save(article);
    }

    public void readAdd(Long id){
        Article article = get(id);
        if(article==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        int times = article.getReadtimes()+1;
        article.setReadtimes(times);
        articleRepository.save(article);
    }

    public Article get(Long id){
        return articleRepository.findById(id);
    }

    public ArticleQo findById(Long id){
        Article article = get(id);
        if(article==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        readAdd(id);
        ArticleQo qo = CopyUtil.copy(article, ArticleQo.class);
        Long timeMillis = 0L;
        if(qo.getCredate()!=null){
            timeMillis = System.currentTimeMillis()-qo.getCredate().getTime();
        }
        qo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
        return qo;
    }

    public Map<String, Object> findPage(ArticleQo queryQo) {
        Page<Article> articlePage = findPageByPredicate(queryQo);
        List<ArticleQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(articlePage.getContent())){
            for (Article article : articlePage.getContent()) {
                ArticleQo qo = CopyUtil.copy(article, ArticleQo.class);
                Long timeMillis = 0L;
                if(qo.getCredate()!=null){
                    timeMillis = System.currentTimeMillis()-qo.getCredate().getTime();
                }
                qo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", articlePage.getTotalPages());
        page.put("totalElements", articlePage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<Article> findPageByPredicate(ArticleQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return articleRepository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getStatus())){
                    predicatesList.add(criteriaBuilder.equal(root.get("status"), queryQo.getStatus()));
                }
                if(CommonUtils.isNotNull(queryQo.getFlag())){
                    predicatesList.add(criteriaBuilder.equal(root.get("flag"), queryQo.getFlag()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
