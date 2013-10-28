##前言
这是一个elasticsearch的中文分词插件，基于ansj，感谢作者阿键，感谢群内热心的朋友。
并宣传一下我们的群QQ211682609

##插件安装

* 第一步，你要有一个`elasticsearch`的服务器(废话)

* 第二步，把代码clone到本地

* 第三步，mvn clean package

* 第四步，进入$Project_Home/target/releases 目录，

* 第五步，拷贝$Project_Home/target/releases/目录下的zip包到任意位置，并解压

* 第六步，将解压后的analysis-ansj拷贝到$ES_HOME/plugins目录下

* 第七步    将解压后的ansj拷贝到$ES_HOME/config目录下

* 第七步，配置分词插件，将下面配置粘贴到，es下config/elasticsearch.yml 文件末尾。
```javascript
################################## ANSJ PLUG CONFIG ################################
index:
  analysis:
    analyzer:
      index_ansj:
          alias: [ansj_index_analyzer]
          type: ansj_index
          #user_path: ansj/user
          #ambiguity: ansj/ambiguity.dic
          #stop_path: ansj/stopLibrary.dic
          redis:
             # pool: 
              #    maxactive: 20
               #   maxidle: 10
                #  maxwait: 100
                 # testonborrow: true
              ip: master.redis.yao.com:6379
              channel: ansj_term
      query_ansj:
          alias: [ansj_index_analyzer]
          type: ansj_query
          #user_path: ansj/user
          #ambiguity: ansj/ambiguity.dic
          #stop_path: ansj/stopLibrary.dic
          redis:
              #pool:
              #maxactive: 20
              #maxidle: 10
              #maxwait: 100
              #testonborrow: true
              ip: master.redis.yao.com:6379
              channel: ansj_term
         
以上配置中redis并不是必需的，user_path可以是一个目录，注释了的都具有默认值，可不配置
如果使用redis功能，请确认一下，在user_path下有ext.dic这个文件

##使用
在mapping中，加入analyzer设置，请注意，分词和索引使用不一样的分词器
```javascript
"byName": {
  "type": "string",
  "index_analyzer": "index_ansj",
  "search_analyzer": "query_ansj"
}
```
可以使用分词器测试接口还看到效果:
```
curl -XGET http://host:9200/_analyze?analyzer=query_ansj&text=视康 隐形眼镜
```
然后通过redis发布一个新词看看
```
redis-cli
publish ansj_term c:视康

```
是不是分词发生了变化
```
redis-cli
publish ansj_term d:视康
```
又回来了


#结束
就写这么多吧，有啥问题，QQ找我
