## 前言
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FNLPchina%2Felasticsearch-analysis-ansj.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FNLPchina%2Felasticsearch-analysis-ansj?ref=badge_shield)


这是一个elasticsearch的中文分词插件，基于ansj,感谢群内热心的朋友。
并宣传一下我们的群QQ211682609


## 插件安装

````
进入es目录执行如下命令

./bin/elasticsearch-plugin install https://github.com/NLPchina/elasticsearch-analysis-ansj/releases/download/v${VERSION}/elasticsearch-analysis-ansj-${VERSION}.0-release.zip
````

老版本请阅读当时的 README。
==========

## 2019年1月20日
+ ansj_seg升级至5.1.6


## 2017年4月9日
+ ansj_seg升级至5.1.1
+ 支持配置同义词词典
+ 支持自定义分词器
+ 支持从http，文件，数据库，jar，class加载词典
+ 热词更新，用http接口取代redis


## 2016年11月11日
+ elasticsearch更新至5.0.0
+ ansj_seg升级至5.0.4
+ 新增配置文件config/ansj.cfg.yml


## 2016年04月16日
+ elasticsearch更新2.3.1
+ ansj_seg升级至3.7.3


## 1770年01月01日
+ elasticsearch更新2.1.1
+ ansj_seg升级至3.5
+ 新增http的_ansj接口，用于查看ansj分词词性
+ 新增http的_cat/ansj接口,作用同上，显示为cat方式
+ 新增http的_cat/[index]/analyze接口，和_analyze作用一样，显示为cat方式
+ 更方便的配置

先来点配置好的示例 ^ ^ 别吐槽我的格式化 (顺便求一个判断字符串中含有几个中文的方法)


## 测试


* 创建测试索引

```linux
curl -X PUT "127.0.0.1:9200/test" -H 'Content-Type: application/json' -d '{
    "settings" : {
        "number_of_shards" : 1,
        "number_of_replicas" : 0

    },
    "mappings" : {
        "test" : {
            "_all" : { "enabled" : false },
            "properties" : {
                "name" : { "type" : "string", "analyzer" : "index_ansj", "search_analyzer" : "query_ansj" }
            }
        }
    }
}'
````

* 添加索引内容

````
curl -X PUT "127.0.0.1:9200/test/test/1" -H 'Content-Type: application/json' -d '{
    "name" : "中国人民万岁",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}'
````

* 查询索引

````
浏览器访问:
http://127.0.0.1:9200/test/test/_search?q=name:%E4%B8%AD%E5%9B%BD
````


* 如果你想把ansj作为你的默认分词需要在elasticsearch.yml加入如下配置:

```yaml

#默认分词器,索引
index.analysis.analyzer.default.type: index_ansj

#默认分词器,查询

index.analysis.analyzer.default_search.type: query_ansj
```



## 关于分词器不得不说的那点小事
````
目前默认内置三个分词器

当然如果你有心仔细观察日志看到了实例化了n多分词器如下

 regedit analyzer named : index_ansj
 regedit analyzer named : query_ansj
 regedit analyzer named : to_ansj
 regedit analyzer named : dic_ansj
 regedit analyzer named : user_ansj
 regedit analyzer named : search_ansj

why????
额 只有三个其他都是别名

````


### 索引分词

```shell

index_ansj 是索引分词,尽可能分词处所有结果 example

http://127.0.0.1:9200/_cat/test/analyze?text=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B8%E8%BD%AF%E8%83%B6%E5%9B%8A&analyzer=index_ansj

六味  		0		2		0		word		
地   		2		3		1		word		
黄丸软 		3		6		2		word		
胶囊  		6		8		3		word		
六味地黄		0		4		4		word		
地黄  		2		4		5		word		
地黄丸 		2		5		6		word		
软胶  		5		7		7		word		
软胶囊 		5		8		8		word			


````


### 搜索分词 (search_ansj=to_ansj=query_ansj)

```shell

query_ansj 是搜索分词,是索引分词的子集,保证了准确率 example

http://127.0.0.1:9200/_cat/test/analyze?text=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B8%E8%BD%AF%E8%83%B6%E5%9B%8A&analyzer=query_ansj

六味 		0		2		0		word		
地  			2		3		1		word		
黄丸软		3		6		2		word		
胶囊 		6		8		3		word		

````

### 用户自定义词典优先的分词方式 (user_ansj=dic_ansj)

```shell

dic_ansj 是用户自定义词典优先策略

http://127.0.0.1:9200/_cat/test/analyze?text=%E5%85%AD%E5%91%B3%E5%9C%B0%E9%BB%84%E4%B8%B8%E8%BD%AF%E8%83%B6%E5%9B%8A&analyzer=dic_ansj

六味地黄		0		4		0		word		
丸   		4		5		1		word		
软胶囊 		5		8		2		word		

````



## 编译安装

* 第一步，你要有一个`elasticsearch`的服务器(废话) 版本2.1.1

* 第二步，把代码clone到本地

* 第三步，mvn clean install

* 第四步，进入$Project_Home/target/releases 目录，

* 第五步，拷贝$Project_Home/target/releases/目录下的zip包到解压到$ES_HOME/plugins目录下



 
现在,你的es集群已经有下面三个名字的analyzer

+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ dic_ansj

三个名字的tokenizer

+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ dic_ansj


## 分词文件配置:
### 2.4.2或5.2.0以上:
- 5.2.0以上配置文件config/ansj.cfg.yml，需要放入$ES_HOME/config/elasticsearch-analysis-ansj/ansj.cfg.yml或者$ES_HOME/plugins/elasticsearch-analysis-ansj-*/config/ansj.cfg.yml
```yaml
# 全局变量配置方式一
ansj:
  #默认参数配置
  isNameRecognition: true #开启姓名识别
  isNumRecognition: true #开启数字识别
  isQuantifierRecognition: true #是否数字和量词合并
  isRealName: false; #是否保留真实词语,建议保留false

  #用户自定词典配置
  dic: default.dic #也可以写成 file//default.dic , 如果未配置dic,则此词典默认加载
  # http方式加载
  #dic_d1: http://xxx/xx.dic
  # jar中文件加载
  #dic_d2: jar://org.ansj.dic.DicReader|/dic2.dic
  # 从数据库中加载
  #dic_d3: jdbc://jdbc:mysql://xxxx:3306/ttt?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull|username|password|select name as name,nature,freq from dic where type=1
  # 从自定义类中加载,YourClas  extends PathToStream
  #dic_d3: class://xxx.xxx.YourClas|ohterparam

  #过滤词典配置
  #stop: http,file,jar,class,jdbc 都支持
  #stop_key1: ...

  #歧义词典配置
  #ambiguity: http,file,jar,class,jdbc 都支持
  #ambiguity_key1: ...

  #同义词词典配置
  #synonyms: http,file,jar,class,jdbc 都支持
  #synonyms_key1: ...

# 全局变量配置方式二 通过配置文件的方式配置,优先级高于es本身的配置
ansj_config: ansj_library.properties # http,file,jar,class,jdbc 都支持,格式参见ansj_library.properties
```
- 配置自定义分词器
```yaml
# 配置自定义分词器
index:
  analysis:
    tokenizer :
      my_dic :
        # 类型支持base_ansj, index_ansj, query_ansj, dic_ansj, nlp_ansj
        type : dic_ansj   
        dic: dic
        stop: stop
        ambiguity: ambiguity
        synonyms: synonyms
        isNameRecognition: true
        isNumRecognition: true
        isQuantifierRecognition: true
        isRealName: false

    analyzer:
      my_dic:
        type: custom
        tokenizer: my_dic
```
- http接口
  + /_cat/ansj: 执行分词
  + /_cat/ansj/config: 显示全部配置
  + /_ansj/flush/config: 刷新全部配置
  + /_ansj/flush/dic: 更新全部词典。包括用户自定义词典,停用词典,同义词典,歧义词典,crf


## 结束
就写这么多吧，有啥问题，QQ找我


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FNLPchina%2Felasticsearch-analysis-ansj.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FNLPchina%2Felasticsearch-analysis-ansj?ref=badge_large)