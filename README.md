##前言
这是一个elasticsearch的中文分词插件，基于ansj，感谢作者阿键，感谢群内热心的朋友。
并宣传一下我们的群QQ211682609

##插件安装

* 第一步，你要有一个`elasticsearch`的服务器(废话)

* 第二步，把代码clone到本地

* 第三步，mvn clean assembly:assembly(破maven。今天把我整崩溃了。发誓以后能不用maven发布项目就不用了。受不了这个破玩意)

* 第四步，进入$Project_Home/target/ 目录，

* 第五步，拷贝$Project_Home/target/elasticsearch-analysis-ansj-0.1-jar-with-dependencies.jar 到es的lib目录中

* 第六步，将$Project_Home/target/config目录合并到es中的config目录

* 第七步，配置分词插件，将下面配置粘贴到，es下config/elasticsearch.yml 文件末尾。
```javascript
################################## ANSJ PLUG CONFIG ################################
index.analysis.analyzer.default.type : org.ansj.elasticsearch.index.analysis.AnsjIndexAnalyzerProvider
#是否提取词干默认为false
ansj_pstemming : false
#用户自定义辞典。如果是目录的话辞典文件必须以dic结尾
ansj_user_path : library/default.dic
#歧义排错
ansj_ambiguity : library/ambiguity.dic
#停用词辞典路径
stop_path : library/stop.dic
```
以上配置中redis并不是必需的，user_path可以是一个目录。
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
