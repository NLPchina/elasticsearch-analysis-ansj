##前言

这是一个elasticsearch的中文分词插件，基于ansj，感谢本人，感谢群内热心的朋友。
并宣传一下我们的群QQ211682609

##版本对应

- plugin        ==>     elasticsearch
- 1.0.0Release  ==>     0.90.2
- master        ==>     1.0.0


==========

##插件安装

进入Elasticsearch目录运行如下命令 

````
./bin/plugin -u http://maven.ansj.org/org/ansj/elasticsearch-analysis-ansj/1.x.1/elasticsearch-analysis-ansj-1.x.1-release.zip -i ansj
````

##下载安装

进入[http://maven.ansj.org/org/ansj/elasticsearch-analysis-ansj/](http://maven.ansj.org/org/ansj/elasticsearch-analysis-ansj/) 直接下 载zip包解压到plugin目录下..


##编译安装

* 第一步，你要有一个`elasticsearch`的服务器(废话)

* 第二步，把代码clone到本地

* 第三步，mvn clean package

* 第四步，进入$Project_Home/target/releases 目录，

* 第五步，拷贝$Project_Home/target/releases/目录下的zip包到任意位置，并解压

* 第六步，将解压后的analysis-ansj拷贝到$ES_HOME/plugins目录下

* 第七步    将解压后的ansj拷贝到$ES_HOME/config目录下

* 第七步，配置分词插件，将下面配置粘贴到，es下config/elasticsearch.yml 文件末尾。



==========

##分词文件配置:

配置文件样例:
```yaml
################################## ANSJ PLUG CONFIG ################################
ndex:
   analysis:
     analyzer:
       index_ansj:
           alias: [ansj_index_analyzer]
           type: ansj_index
           is_name: false
           redis:
               pool:
                   maxactive: 20
                   maxidle: 10
                   maxwait: 100
                   testonborrow: true
               ip: master.redis.yao.com:6379
               channel: ansj_term
       query_ansj:
           alias: [ansj_query_analyzer]
           type: ansj_query
           is_name: false
           redis:
               pool:
                   maxactive: 20
                   maxidle: 10
                   maxwait: 100
                   testonborrow: true
               ip: master.redis.yao.com:6379
               channel: ansj_term
       customer_ansj_index:
           tokenizer: index_ansj_token
           filter: [sysfilter]
       customer_ansj_query:
           tokenizer: query_ansj_token
           filter: [sysfilter]
     tokenizer:
        index_ansj_token:
            type: ansj_index_token
            is_name: false
            is_num: false
            is_quantifier: false
            redis:
                pool:
                    maxactive: 20
                    maxidle: 10
                    maxwait: 100
                    testonborrow: true
                ip: master.redis.yao.com:6379
                channel: ansj_term
        query_ansj_token:
            type: ansj_query_token
            is_name: false
            is_num: false
            is_quantifier: false
            redis:
                pool:
                    maxactive: 20
                    maxidle: 10
                    maxwait: 100
                    testonborrow: true
                ip: master.redis.yao.com:6379
                channel: ansj_term
      filter:
        sysfilter:
            type: synonym
            synonyms:
                - 片,颗 =>粒
```

人名识别,建议先去看看我发表的[问题](http://es-bbs.medcl.net/discussion/514/onni%E9%98%BF%E9%94%AEm%E5%A4%A7%E5%BF%AB%E6%9D%A5%E7%9C%8B%E7%9C%8B%E5%90%A7%E5%85%B3%E4%BA%8Equery_string%E5%A6%82%E4%BD%95%E5%B0%86query%E5%8F%82%E6%95%B0%E5%81%9A%E4%B8%BA%E4%B8%80%E4%B8%AA%E6%95%B4%E4%BD%93%E5%BC%95%E5%8F%91%E7%9A%84%E8%A1%80%E6%A1%88%E6%8D%AE%E8%AF%B4%E6%A0%87%E9%A2%98%E8%A6%81%E9%86%92%E7%9B%AE)

如果有和我一样问题的同学，建议关闭人名识别
以上配置中redis并不是必需的，user_path可以是一个目录，注释了的都具有默认值，可不配置
如果使用redis功能，请确认一下，在user_path下有ext.dic这个文件

如果你的log日志中出现如下字样，恭喜你，成功了。(日志在$ES_HOME/logs下，哪个文件，当然就是你的集群名称啦，知道的无视这段吧)


```
[2013-10-25 18:23:55,427][INFO ][ansj-analyzer            ] ansj停止词典加载完毕!
[2013-10-25 18:24:01,509][INFO ][ansj-analyzer            ] ansj分词器预热完毕，可以使用!
[2013-10-25 18:24:01,523][INFO ][ansj-redis-pool          ] master.redis.yao.com:6379
[2013-10-25 18:24:01,607][INFO ][ansj-analyzer            ] redis守护线程准备完毕,ip:master.redis.yao.com:6379,port:6379,channel:ansj_term
[2013-10-25 18:24:01,617][INFO ][ansj-redis-msg           ] subscribe channel:ansj_term and subscribedChannels:1
```

##使用

在mapping中，加入analyzer设置和tokenizer设置，请注意，分词和索引使用不一样的分词器

```javascript
"byName": {
  "type": "string",
  "index_analyzer": "index_ansj",
  "search_analyzer": "query_ansj"
},
"name": {
  "type": "string",
  "index_analyzer": "customer_ansj_index",
  "search_analyzer": "customer_ansj_query"
},
```

可以使用分词器测试接口还看到效果:

```
curl -XGET http://host:9200/_analyze?analyzer=query_ansj&text=视康 隐形眼镜
```

然后通过redis发布一个新词看看

```
redis-cli
publish ansj_term u:c:视康

```

是不是分词发生了变化

```
redis-cli
publish ansj_term u:d:视康
```

又回来了

然后通过redis发布一个歧义词

```
redis-cli
publish ansj_term a:c:减肥瘦身-减肥,nr,瘦身,v
```

是不是分词发生了变化

```
redis-cli
publish ansj_term a:d:减肥瘦身
```

又回来了


## 结束
就写这么多吧，有啥问题，QQ找我 
