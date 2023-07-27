package com.example.gulimall.product.service.impl;

import com.example.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.example.gulimall.product.entity.AttrEntity;
import com.example.gulimall.product.service.AttrAttrgroupRelationService;
import com.example.gulimall.product.service.AttrService;
import com.example.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.example.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.AttrGroupDao;
import com.example.gulimall.product.entity.AttrGroupEntity;
import com.example.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    private IPage<AttrGroupEntity> page;
    @Lazy
    @Autowired
    AttrService attrService;
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        // select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like %key%
        //在指定分类中查询
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatId(Long catelogId) {
        //查出当前分类下所有属性分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //查出属性分组与属性的关联
        List<Long> gids = groupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", gids));
        Map<Long, List<AttrAttrgroupRelationEntity>> gidMap = relationEntities.stream().collect(Collectors.groupingBy(entity -> entity.getAttrGroupId()));
        //查出关联属性
        List<Long> attrIds = relationEntities.stream().map(entity -> entity.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrService.list(new QueryWrapper<AttrEntity>().in("attr_id", attrIds));
        Map<Long, List<AttrEntity>> aidMap = attrEntities.stream().collect(Collectors.groupingBy(AttrEntity::getAttrId));
        //属性分组-属性分组与属性关联-属性
        List<AttrGroupWithAttrsVo> voList = groupEntities.stream().map(group -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, vo);
            List<AttrEntity> attrs = gidMap.get(group.getAttrGroupId()).stream().flatMap(entity -> aidMap.get(entity.getAttrId()).stream()).collect(Collectors.toList());
            vo.setAttrs(attrs);
            return vo;
        }).collect(Collectors.toList());
        return voList;
    }

    @Override
    public List<SkuItemVo.SpuAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long catalogId, Long spuId) {
        return baseMapper.getAttrGroupWithAttrsBySpuId(catalogId, spuId);
    }
}