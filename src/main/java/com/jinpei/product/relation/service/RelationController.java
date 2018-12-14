package com.jinpei.product.relation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品关联关系控制器类
 * Created by liuzhaoming on 2018/12/13.
 */
@RestController
@RequestMapping("/api/relations")
public class RelationController {

    @Autowired
    private RelationService relationService;

    /**
     * 分页查询所有商品关联关系
     *
     * @param page 页码，从0开始
     * @param size 每页数据条数
     * @return 商品关联关系列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<RelationRule> query(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        return relationService.query(page, size);
    }

    /**
     * 根据ID查询该商品的所有关联关系
     *
     * @param productId 商品ID
     * @return 该商品关联的关联关系列表
     */
    @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
    public List<RelationRule> queryByProductId(@PathVariable String productId) {
        return relationService.query(productId);
    }
}
