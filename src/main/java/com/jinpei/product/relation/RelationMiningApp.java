package com.jinpei.product.relation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品关联关系挖掘APP
 * Created by liuzhaoming on 2018/12/13.
 */
@SpringBootApplication
@Slf4j
public class RelationMiningApp {
    public static void main(String[] args) {
        SpringApplication.run(RelationMiningApp.class, args);
        log.info("---------------------------------");
        log.info("Finish to start application !");
        log.info("---------------------------------");
    }
}
