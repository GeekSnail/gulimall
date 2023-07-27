package com.example.gulimall.product.web;

import com.example.gulimall.product.entity.CategoryEntity;
import com.example.gulimall.product.service.CategoryService;
import com.example.gulimall.product.vo.CatalogVo;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model, HttpSession session) {
        List<CategoryEntity> categoryEntities = categoryService.listByLevel(1);
        model.addAttribute("categories", categoryEntities);
//        System.out.println(categoryEntities);
        return "index";
    }
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<CatalogVo>> getCatalogs() {
        Map<String, List<CatalogVo>> map = categoryService.getCatalogJson();
        return map;
    }

    @Autowired
    RedissonClient redissonClient;
    @ResponseBody
    @GetMapping("/redisson")
    public String testRedisson() {
        RLock lock = redissonClient.getLock("myLock");
        lock.lock(); //阻塞式等待 默认30s
        try {
            System.out.println(Thread.currentThread().getId() + " locked");
            Thread.sleep(31000);
            System.out.println(Thread.currentThread().getId() + " finished");
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getId() + " unlocked");
        }
        return "redisson";
    }
    @ResponseBody
    @GetMapping("/park/in")
    public String parkIn() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.acquire();
        return "park ok";
    }
    @ResponseBody
    @GetMapping("/park/out")
    public String parkOut() throws InterruptedException {
        RSemaphore space = redissonClient.getSemaphore("park");
        space.release();
        return "release ok";
    }
    @ResponseBody
    @GetMapping("/door")
    public long doorStatus() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(2);
        door.await();
        return door.getCount();
    }
    @ResponseBody
    @PostMapping("/door")
    public long doorCountDown() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        return door.getCount();
    }
}
