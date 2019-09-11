# 基于 FP-Growths 算法的购物车商品关联关系挖掘

## 项目介绍

我们浏览电商网站时，经常出现一些购买套装、买了 XX 的人还买了 XX 之类的推荐。推荐算法有很多，这里我们使用 [FP-Growths算法](https://www.cs.sfu.ca/~jpei/publications/sigmod00.pdf) 挖掘用户购物车。

项目基于 JAVA 语言开发，使用 Spring Boot 开发框架和 Spark MLlib 机器学习框架，以 RESTful 接口的方式对外提供服务。

## 软件架构

项目的架构如下：

![架构图](https://s2.ax1x.com/2019/09/11/nwCA76.jpg)

## 安装教程

### 1. 安装并运行 Spark

项目使用的 Spark 版本为 2.2.1，详情见 [Spark 安装使用说明](https://spark.apache.org/docs/2.2.1/spark-standalone.html)。

### 2. 下载源码

``` shell
  $ cd  {relation_project_home}
  $ git clone https://github.com/jingpeicomp/product-relation-mining.git
```

### 3. 修改参数配置

项目的配置文件见 {relation_project_home}/src/main/java/resources/application.properties。

配置名 | 值 |  说明
:----------- | :-----------| :-----------
server.port     | 默认值 8083    | Web 应用对外服务端口
relation.dataPath    | {relation_project_home}/data         | 数据文件目录路径，包含购物车数据和商品数据
relation.modelPath    | {relation_project_home}/model         | 机器学习模型文件目录路径
category.spark.masterUrl     | 如果是 Spark 单 Standalone 安装方式的话，默认地址是spark://localhost:7077    | Spark 集群 master url
category.spark.dependenceJar     | {relation_project_home}/target/product-relation-mining-1.0.0-SNAPSHOT-jar-with-dependencies.jar    | Spark App 依赖的jar文件
category.spark.properties.****     | 无    | 以category.spark.properties.开头的属性都是 Spark 配置参数，最终都会设置到 Spark App 上。不同 Spark 部署方式对应的属性不同，详情见 [Spark 配置参数说明](https://spark.apache.org/docs/2.2.1/configuration.html)。[现有的配置文件](src/main/java/resources/application.properties)是针对 Standalone 部署方式的参数。Spark 最重要的配置参数是 CPU 和内存资源的设定。

### 4. 通过 maven 打包

```shell
  $ cd  {relation_project_home}
  $ mvn clean package -Dmaven.test.skip=true
```

项目使用了 jar-with-dependencies 和 Spring Boot 打包插件，最后在目录 {relation_project_home}/target 生成三个jar文件：

* original-product-relation-mining-1.0.0-SNAPSHOT.jar 是项目源码 jar；

* product-relation-mining-1.0.0-SNAPSHOT-jar-with-dependencies.jar 是包含了所有依赖 jar，作为 Spark 应用的依赖 jar，提交到 Spark 集群上；

* product-relation-mining-1.0.0-SNAPSHOT.jar是 Spring Boot 可运行 jar；

## 使用说明

### 启动应用

```shell
  $ cd  {relation_project_home}
  $ chmod a+x target/product-relation-mining-1.0.0-SNAPSHOT.jar
  $ java -jar target/product-relation-mining-1.0.0-SNAPSHOT.jar
```

由于项目基于 Spring Boot 框架，因此 Spring Boot 所有的启动参数都适用于本项目。

出现如下日志则代表应用启动成功：

```log
---------------------------------
Finish to start application !
---------------------------------
```

### 启动模型训练

项目启动时会判断 [application.properties](src/main/java/resources/application.properties) 中 `relation.modelPath` 参数配置的模型文件目录是否存在模型，如果没有模型，则会启动模型的训练。

### 模型和样本数据

#### 模型数据

项目 [model](/model) 目录已经附上了本地训练好的一个模型，可以直接使用。

#### 购物车数据

项目附上了一个简单的测试训练样本 [data/shoppingCart.data](data/shoppingCart.data) ，可以用来测试。因为样本较小的关系，训练出来的模型的准确率会很低。

训练数据的一行表示一个用户的购物车，商品ID之间用 " " 分隔。

```text
35 193 1019 812 684
620 338 239 580 469 241 438 699 1083 764 209 227 551 147 788 668
856 496 695 459 690 364
```

#### 商品数据

商品数据 [data/product.data](data/product.data) ，格式为 `{商品ID}|&|{商品名称}` 。

```text
982|&|UYEKI威奇 家用床上除螨虫防螨 W双效升级版 250ml
983|&|RECIPE/莱斯璧水晶防晒喷雾150ml/瓶
984|&|papa recipe春雨蜂蜜美白面膜 10片/盒
```

### RESTful 接口

#### 1. 查询商品关联关系

| URL        | HTTP           | 功能  |  
| ------------- |-------------| -----|  
| /api/relations      | GET | 返回所有关联关系 |

##### 请求参数

| 参数名        | 数据类型           | 可需  |   描述 |  
| ------------- |-------------| -----|  ---------|  
| page | int | 选填 | 页码，从0开始， 默认0 |
| size | int | 选填 | 每页数据条数， 默认50 |

##### 请求结果

> http://localhost:8083/api/relations

```json

{
    "size": 42,
    "data": [
        {
            "antecedentId": "690",
            "consequentId": "459",
            "confidence": 0.88,
            "antecedentName": "DAISO大创 ER胎盘素保湿滋润精华爽肤水 120ml/瓶",
            "consequentName": "DAISO大创 ER胎盘素淡斑保湿精华乳液 120ml/瓶",
            "antecedentCustomerNum": 25
        }
    ]
}

```

> * size 商品关联关系总数目。
> * antecedentId 前项商品ID
> * consequentId 关联商品ID
> * confidence  关联关系的置信度
> * antecedentName 前项商品名称
> * consequentName 关联商品名称
> * antecedentCustomerNum 购物车包含前项商品的用户数

**以上面的关联关系为例进行说明：有 25 个用户购物车中包含商品 690（ DAISO大创 ER胎盘素保湿滋润精华爽肤水 120ml/瓶 ）， 其中的 22 个用户购物车还同时包含商品 459（ DAISO大创 ER胎盘素淡斑保湿精华乳液 120ml/瓶 ）， 同时包含的概率为 88% ( 22/25 = 0.88 )。因此商品 690 和 459 关联关系是很强的，置信度为 88%。**

#### 2. 查询指定商品的关联关系

| URL        | HTTP           | 功能  |  
| ------------- |-------------| -----|  
| /api/relations/:id      | GET | 返回指定商品的关联关系 |

##### 请求参数

| 参数名        | 数据类型           | 可需  |   描述 |  
| ------------- |-------------| -----|  ---------|  
| id | string | 必填 | 商品ID |

##### 请求结果

> http://localhost:8083/api/relations/690

```json
[
    {
        "antecedentId": "690",
        "consequentId": "459",
        "confidence": 0.88,
        "antecedentName": "DAISO大创 ER胎盘素保湿滋润精华爽肤水 120ml/瓶",
        "consequentName": "DAISO大创 ER胎盘素淡斑保湿精华乳液 120ml/瓶",
        "antecedentCustomerNum": 25
    },
    {
        "antecedentId": "690",
        "consequentId": "496",
        "confidence": 0.76,
        "antecedentName": "DAISO大创 ER胎盘素保湿滋润精华爽肤水 120ml/瓶",
        "consequentName": "DAISO大创 ER胎盘素淡斑保湿精华液 30ml/瓶",
        "antecedentCustomerNum": 25
    }
]
```