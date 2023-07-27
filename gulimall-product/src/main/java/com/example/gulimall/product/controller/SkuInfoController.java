package com.example.gulimall.product.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.service.SkuInfoService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * sku信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-03-28 00:49:04
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R page(@RequestParam Map<String, Object> params) {
//        PageUtils page = skuInfoService.queryPage(params);
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/listby")
    public R list(@RequestParam("skuId") List<Long> skuIds) {
        List<SkuInfoEntity> entities = skuInfoService.listByIds(skuIds);
        return R.ok().put("data", entities);
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId) {
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    @GetMapping("/price")
    public Map<Long,BigDecimal> getPriceByIds(@RequestParam("skuIds") List<Long> skuIds) {
//        boolean b = Pattern.matches("^(\\d+,?)+$", skuIds);
        List<SkuInfoEntity> entities = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().in("sku_id", skuIds));
        if (entities!=null && entities.size() > 0) {
            return entities.stream().collect(Collectors.toMap(e->e.getSkuId(), e->e.getPrice()));
        }
        return null;
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
