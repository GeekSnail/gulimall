package com.example.gulimall.coupon.service.impl;

import com.example.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.example.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.coupon.dao.SeckillSessionDao;
import com.example.gulimall.coupon.entity.SeckillSessionEntity;
import com.example.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> listLatest(int days) {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now().plusDays(days-1), LocalTime.MAX);
        start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", start, end));
        if (list != null && list.size() > 0) {
            List<Long> ids = list.stream().map(s -> s.getId()).collect(Collectors.toList());
            Map<Long,List<SeckillSkuRelationEntity>> relationMap = new HashMap<>();
            List<SeckillSkuRelationEntity> skuRelations = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().in("promotion_session_id", ids));
            skuRelations.forEach(r -> {
                Long sid = r.getPromotionSessionId();
                if (relationMap.containsKey(sid))
                    relationMap.get(sid).add(r);
                else
                    relationMap.put(sid, new ArrayList<>(List.of(r)));
            });
            list = list.stream().map(session -> {
                session.setSkuRelations(relationMap.get(session.getId()));
                return session;
            }).collect(Collectors.toList());
        }
        return list;
    }

}