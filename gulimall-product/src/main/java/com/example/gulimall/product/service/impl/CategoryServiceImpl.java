package com.example.gulimall.product.service.impl;

import com.example.gulimall.product.service.CategoryBrandRelationService;
import com.example.gulimall.product.vo.CatalogVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.CategoryDao;
import com.example.gulimall.product.entity.CategoryEntity;
import com.example.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> list = baseMapper.selectList(null);
        //2.组装成父子树形结构
        List<CategoryEntity> list1 = list.stream().filter(entity -> entity.getParentCid() == 0)
                .map(entity -> {
                    entity.setChildren(getChildren(entity, list));
                    return entity;
                })
                .sorted((e1, e2) -> (e1.getSort()==null?0:e1.getSort())-(e2.getSort()==null?0:e2.getSort()))
                .collect(Collectors.toList());
        return list1;
    }

    @Override
    public void removeCatByIds(List<Long> asList) {
        //TODO 检查待删除的菜单是否被别处引用
        baseMapper.deleteBatchIds(asList);
    }

    //[2,34,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        CategoryEntity entity = this.getById(catelogId);
        paths.add(catelogId);
        while (entity.getParentCid() != 0) {
            entity = this.getById(entity.getParentCid());
            paths.add(entity.getCatId());
        }
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联数据
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value="category",key="'listByLevel'"),
//            @CacheEvict(value="category",key="'getCatalogJson'"),
//    })
    @CacheEvict(value="category", allEntries = true)
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value={"category"}, key="#root.methodName")
    @Override
    public List<CategoryEntity> listByLevel(int level) {
//        System.out.println("listByLevel1");
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", level));
        return list;
    }

    @Cacheable(value="category", key="#root.methodName")
    @Override
    public Map<String, List<CatalogVo>> getCatalogJson() {
        System.out.println("查询数据库..." + "-" + Thread.currentThread().getId());
        List<CategoryEntity> list = baseMapper.selectList(null);
        Map<String, List<CatalogVo>> map = list.stream().filter(e -> e.getCatLevel() == 1)
            .collect(Collectors.toMap(l1 -> l1.getCatId()+"", l1 ->
                list.stream().filter(e -> e.getParentCid() == l1.getCatId()).map(l2 -> {
                    List<CatalogVo> l3List = list.stream().filter(e -> e.getParentCid() == l2.getCatId()).map(l3 ->
                        new CatalogVo(l3.getCatId() + "", l3.getName(), l2.getCatId() + "", null)
                    ).collect(Collectors.toList());
                    return new CatalogVo(l2.getCatId() + "", l2.getName(), l1.getCatId() + "", l3List);
                }).collect(Collectors.toList())
            ));
        return map;
    }

    public Map<String, List<CatalogVo>> getCatalogJsonWithRedisson() {
        RLock lock = redissonClient.getLock("catalogJSON-lock");
        lock.lock();
        Map<String, List<CatalogVo>> map;
        try {
            map = getCatalogAndSetToRedis("catalogJSON");
        } finally {
            lock.unlock();
        }
        return map;
    }

    public Map<String, List<CatalogVo>> getCatalogJsonWithRedisLock() {
        ValueOperations ops = redisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        String lkey = "lock";
        Boolean locked;
        while (!(locked = ops.setIfAbsent(lkey, uuid, 300, TimeUnit.SECONDS))) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Map<String, List<CatalogVo>> map;
        try {
            map = getCatalogAndSetToRedis("catalogJSON");
        } finally {
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then return redis.call(\"del\",KEYS[1])\n" +
                            "else return 0 end";
            Long delRes = (Long) redisTemplate.execute(RedisScript.of(script, Long.class), Arrays.asList(lkey), uuid);
        }
        return map;
    }

    public Map<String, List<CatalogVo>> getCatalogJsonWithLocalLock() {
        String key = "catalogJSON";
        Map<String, List<CatalogVo>> map = (Map<String, List<CatalogVo>>) redisTemplate.opsForValue().get(key);
        if (map == null) {
            synchronized (this) {
                map = getCatalogAndSetToRedis(key);
            }
        }
        return map;
    }

    public Map<String, List<CatalogVo>> getCatalogAndSetToRedis(String key) {
        ValueOperations ops = redisTemplate.opsForValue();
        Map<String, List<CatalogVo>> map = (Map<String, List<CatalogVo>>) ops.get(key);
        if (map == null) {
            System.out.println("查询数据库..." + "-" + Thread.currentThread().getId());
            List<CategoryEntity> list = baseMapper.selectList(null);
            map = list.stream().filter(e -> e.getCatLevel() == 1)
                .collect(Collectors.toMap(l1 -> l1.getCatId()+"", l1 ->
                    list.stream().filter(e -> e.getParentCid() == l1.getCatId()).map(l2 -> {
                        List<CatalogVo> l3List = list.stream().filter(e -> e.getParentCid() == l2.getCatId()).map(l3 ->
                            new CatalogVo(l3.getCatId() + "", l3.getName(), l2.getCatId() + "", null)
                        ).collect(Collectors.toList());
                        return new CatalogVo(l2.getCatId() + "", l2.getName(), l1.getCatId() + "", l3List);
                    }).collect(Collectors.toList())
                ));
            ops.set(key, map, 1, TimeUnit.DAYS);
        }
        return map;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> list1 = list.stream().filter(entity -> entity.getParentCid().equals(root.getCatId()))
                .map(entity -> {
                    entity.setChildren(getChildren(entity, list));
                    return entity;
                })
                .sorted((e1, e2) -> (e1.getSort()==null?0:e1.getSort())-(e2.getSort()==null?0:e2.getSort()))
                .collect(Collectors.toList());
        return list1;
    }

    private List<CategoryEntity> getCategoryTree(List<CategoryEntity> list) {
        List<CategoryEntity> list1 = new ArrayList<>();
        List<CategoryEntity> list2 = new ArrayList<>();
        List<CategoryEntity> list3 = new ArrayList<>();
        for (CategoryEntity entity: list) {
            if (entity.getCatLevel() == 1)
                list1.add(entity);
            else if (entity.getCatLevel() == 2) {
                list2.add(entity);
            } else if (entity.getCatLevel() == 3) {
                list3.add(entity);
            }
        }
        list1.stream().forEach(entity -> {
            entity.setChildren(list2.stream().filter(entity1 -> entity.getParentCid().equals(entity.getCatId()))
                    .sorted((e1, e2) -> e1.getSort()-e2.getSort()).collect(Collectors.toList()));
        });
        list2.stream().forEach(entity -> {
            entity.setChildren(list3.stream().filter(entity1 -> entity.getParentCid().equals(entity.getCatId()))
                    .sorted((e1, e2) -> e1.getSort()-e2.getSort()).collect(Collectors.toList()));
        });
        return list1;
    }
}