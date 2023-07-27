package com.example.gulimall.product;

//import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.gulimall.product.entity.BrandEntity;
import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.service.*;
import com.example.gulimall.product.vo.SkuImageVo;
import com.example.gulimall.product.vo.SkuItemVo;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("Apple");
//        brandService.save(brandEntity);
//        System.out.println(brandEntity);
//
//        brandEntity.setDescript("苹果");
//        brandService.updateById(brandEntity);
//        System.out.println(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(System.out::println);
    }

//    @Autowired
//    OSSClient ossClient;
//    @Test
//    public void ossput() throws FileNotFoundException {
//        String bucketName = "202304";
//        String key = "20150708103810_comps.jpg";
//        FileInputStream inputStream = new FileInputStream("C:\\Users\\35398\\Pictures\\插画\\"+key);
//        ossClient.putObject(bucketName, key, inputStream);
//        System.out.println("finished.");
//    }

    @Autowired
    CategoryService categoryService;
    @Test
    public void testFindCatelogPath() {
        Long[] path = categoryService.findCatelogPath(225L);
        System.out.println(path);
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world_"+ UUID.randomUUID());
        System.out.println(ops.get("hello"));

        SkuImageVo vo = new SkuImageVo();
        vo.setImgUrl("http://xxx");
        vo.setDefaultImg(1);
        ValueOperations<String, SkuImageVo> ops1 = redisTemplate.opsForValue();
        ops1.set("object", vo);
        SkuImageVo obj = ops1.get("object");
        if (obj != null) {
            System.out.println(obj.getDefaultImg() + obj.toString());
        }
    }

    @Autowired
    RedissonClient redissonClient;
    @Test
    public void testRWLock() throws InterruptedException {
        Runnable wtask = () -> {
            System.out.println(writeValue());
        };
        Runnable rtask = () -> {
            System.out.println(readValue());
        };
        new Thread(wtask).start();
        new Thread(rtask).start();
        Thread rt = new Thread(rtask);
        rt.start();
        rt.join();
    }

    public String writeValue() {
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("rwlock");
        RLock rLock = rwlock.writeLock();
        rLock.lock();
        System.out.println("write " + Thread.currentThread().getId());
        String s = UUID.randomUUID().toString();
        try {
            Thread.sleep(5000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }
    public String readValue() {
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("rwlock");
        RLock rLock = rwlock.readLock();
        rLock.lock();
        System.out.println("read " + Thread.currentThread().getId());
        String s = "";
        try {
            s = (String) redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @Test
    public void testCountDownLatch() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(2);
        Runnable wentOut = () -> {
            door.countDown();
            System.out.println(Thread.currentThread().getId() + " 已离开");
        };
        new Thread(wentOut).start();
        new Thread(wentOut).start();
        door.await();
        System.out.println("剩余" + door.getCount() + "人");
        System.out.println("关门..");
    }

    @Autowired
    AttrGroupService attrGroupService;
    @Test
    public void getAttrGroupWithAttrsBySpuId() {
        List<SkuItemVo.SpuAttrGroupVo> group = attrGroupService.getAttrGroupWithAttrsBySpuId(225L, 3L);
        System.out.println(group);
    }
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Test
    public void getBySpuId() {
        List<SkuItemVo.SkuSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getBySpuId(3L);
        System.out.println(saleAttrVos);
    }
}
