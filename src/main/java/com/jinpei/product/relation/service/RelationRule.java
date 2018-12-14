package com.jinpei.product.relation.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品之间的关联规则
 * Created by liuzhaoming on 2017/8/30.
 */
@Data
@NoArgsConstructor
public class RelationRule implements Serializable, Comparable<RelationRule> {

    /**
     * 前项商品ID
     */
    private String antecedentId;

    /**
     * 后项商品ID
     */
    private String consequentId;

    /**
     * 规则的置信度
     */
    private Double confidence;

    /**
     * 前项商品名称
     */
    private String antecedentName;

    /**
     * 后项商品名称
     */
    private String consequentName;

    /**
     * 前项商品收藏用户数
     */
    private int antecedentCustomerNum;

    public RelationRule(String antecedentId, String consequentId, Double confidence) {
        this.antecedentId = antecedentId;
        this.consequentId = consequentId;
        this.confidence = confidence;
    }


    @Override
    public int compareTo(RelationRule o) {
        if (null == o) {
            return 1;
        }

        int result = Double.compare(confidence, o.confidence);
        if (result == 0) {
            result = Integer.compare(antecedentCustomerNum, o.antecedentCustomerNum);
        }

        return result;
    }
}
