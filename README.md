##前言

这是一个elasticsearch的中文分词插件，基于ansj,感谢群内热心的朋友。
并宣传一下我们的群QQ211682609

## 版本对应

| plugin        |     elasticsearch|
| --------      |       -----:  |
| 1.0.0  |     0.90.2    |
| 1.x        |     1.x     |
| 2.1.1        |     2.1.1     |
| master        |     2.1.1     |



##插件安装

进入Elasticsearch目录运行如下命令 

````
进入es目录执行如下命令

./bin/plugin install https://github.com/NLPchina/elasticsearch-analysis-ansj/releases/download/v2.1.1/elasticsearch-analysis-ansj-2.1.1-release.zip
````


==========

## 此次更新
+ elasticsearch更新2.1.1
+ ansj_seg升级至3.5
+ 新增http的_ansj接口，用于查看ansj分词词性
+ 新增http的_cat/ansj接口,作用同上，显示为cat方式
+ 新增http的_cat/[index]/analyze接口，和_analyze作用一样，显示为cat方式
+ 更方便的配置

先来点配置好的示例 ^ ^ 别吐槽我的格式化 (顺便求一个判断字符串中含有几个中文的方法)

```shell
✘  ~  curl -XGET http://127.0.0.1:9200/_cat/test/analyze\?text\=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B86%E9%A2%97\&analyzer\=customer_ansj_query\&v
term		start_offset		end_offset		position		type
六味		0		2		0		word
地黄		2		4		1		word
丸		4		5		2		word
6		5		6		3		word
粒		6		7		4		SYNONYM
```

```shell
~  curl -XGET http://127.0.0.1:9200/_cat/ansj\?text\=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B86%E9%A2%97\&analyzer\=customer_ansj_query\&v
name		real_name		nature		offset
六味		六味		nz		0
地黄		地黄		n		2
丸		丸		ng		4
6		6		m		5
颗		颗		q		6
六味地黄		六味地黄		nhm		0
地黄丸		地黄丸		nz		2
```

```shell
~  curl -XGET http://127.0.0.1:9200/_ansj\?text\=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B86%E9%A2%97\&analyzer\=index_ansj\&v
{"terms":[{"name":"六味","real_name":"六味","nature":"nz","offset":0},{"name":"地黄","real_name":"地黄","nature":"n","offset":2},{"name":"丸","real_name":"丸","nature":"ng","offset":4},{"name":"6","real_name":"6","nature":"m","offset":5},{"name":"颗","real_name":"颗","nature":"q","offset":6},{"name":"六味地黄","real_name":"六味地黄","nature":"nhm","offset":0},{"name":"地黄丸","real_name":"地黄丸","nature":"nz","offset":2}]}%
```

## 编译安装

* 第一步，你要有一个`elasticsearch`的服务器(废话) 版本2.1.1

* 第二步，把代码clone到本地

* 第三步，mvn clean install

* 第四步，进入$Project_Home/target/releases 目录，

* 第五步，拷贝$Project_Home/target/releases/目录下的zip包到解压到$ES_HOME/plugins目录下



 
现在,你的es集群已经有下面三个名字的analyzer

+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ user_ansj

三个名字的tokenizer

+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ user_ansj


## 分词文件配置:
在这里我说一下，在插件里我写了一些默认配置，如果你也可以接受我的默认配置，关于ansj就完全不用配置了，或者只修改你需要的配置。下面的代码目录都是相对es的config目录，有几点需要注意一下:

+ ansj的核心词典是和插件一起安装的在插件目录下面
+ redis使用的jar用默认脚本启动会有权限问题，此问题目前只能加./elasticsearch -Des.security.manager.enabled=false参数来解决 如果谁有更好的方法请q我
+ 因为要使用redis的pubsub功能，需要关闭es的权限控制，请慎重使用。

```yaml
## ansj配置
ansj:
 dic_path: "ansj/dic/user/" ##用户词典位置
 ambiguity_path: "ansj/dic/ambiguity.dic" ##歧义词典
 enable_name_recognition: true ##人名识别
 enable_num_recognition: true ##数字识别
 enable_quantifier_recognition: false ##量词识别
 enabled_stop_filter: true ##是否基于词典过滤
 stop_path: "ansj/dic/stopLibrary.dic" ##停止过滤词典
## redis 不是必需的
 redis:
  pool:
   maxactive: 20
   maxidle: 10
   maxwait: 100
   testonborrow: true
  ip: 10.0.85.51:6379
  channel: ansj_term ## publish时的channel名称
  write:
    dic: "ext.dic" ##如果有使用redis的pubsub方式更新词典，默认使用 这个目录是相对于dic_path
```

现在让我们配置几个分词器看看:

```yaml
index:
  analysis:
    analyzer:
      customer_ansj_index:
        tokenizer: index_ansj
        filter: [sysfilter]
      customer_ansj_query:
        tokenizer: query_ansj
        filter: [sysfilter]
    filter:
      sysfilter:
        type: synonym
        synonyms:
          - 片,颗 =>粒
```

## 测试


* 创建测试索引

```linux
curl -XPUT localhost:9200/test -d '{
    "settings" : {
        "number_of_shards" : 1,
        "number_of_replicas" : 0

    },
    "mappings" : {
        "type1" : {
            "_all" : { "enabled" : false },
            "properties" : {
                "name" : { "type" : "string", "analyzer" : "customer_ansj_index", "search_analyzer" : "customer_ansj_query" }
            }
        }
    }
}'
```

* 查询分词

可以使用开头我提供的http接口来查看分词效果

然后通过redis发布一个新词看看
追加新词
```
redis-cli
publish ansj_term u:c:视康

```

是不是分词发生了变化
删除词条
```
redis-cli
publish ansj_term u:d:视康
```

又回来了

然后通过redis发布一个歧义词
追加歧义词
```
redis-cli
publish ansj_term a:c:减肥瘦身-减肥,nr,瘦身,v
```

是不是分词发生了变化
删除歧义词
```
redis-cli
publish ansj_term a:d:减肥瘦身
```

又回来了


## 结束
就写这么多吧，有啥问题，QQ找我
