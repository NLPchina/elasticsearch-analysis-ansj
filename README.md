# elasticsearch-analysis-ansj: elasticsearch 的中文分词插件

# 前言
elasticsearch-analysis-ansj 是一个基于 [ansj](https://github.com/NLPchina/ansj_seg) 分词算法 的 elasticsearch 的中文分词插件。

# 编译
```bash
mvn package
```
编译成功后，将会生成打包好的插件压缩包：`target/releases/elasticsearch-analysis-ansj-<版本号>-release.zip`。

# 安装
## 安装命令
在 es 安装目录下执行下面的命令安装插件：
```bash
./bin/elasticsearch-plugin install file:///<你的路径>/elasticsearch-analysis-ansj-<版本号>-release.zip
```

> 安装完成后，会生成一个默认的配置文件： `<ES_HOME>/config/elasticsearch-analysis-ansj/ansj.cfg.yml`，根据需要修改此文件即可。

## 测试
安装完成后，启动 es 集群。通过以下方式测试是否安装正确：  
**方法一：**  
通过 `kibana` 执行 `GET /_cat/ansj?text=中国&type=index_ansj` 命令，测试 `index_ansj` 分词器，返回内容如下：
```json
{
  "result": [
    {
      "name": "中国",
      "nature": "ns",
      "offe": 0,
      "realName": "中国",
      "synonyms": null
    },
    {
      "name": "中",
      "nature": "f",
      "offe": 0,
      "realName": "中",
      "synonyms": null
    },
    {
      "name": "国",
      "nature": "n",
      "offe": 1,
      "realName": "国",
      "synonyms": null
    }
  ]
}
```
**方法二：**  
通过 `kibana` 执行 `GET /_cat/ansj/config` 命令，获取配置文件内容如下：
```json
{
  "ambiguity": [
    "ambiguity"
  ],
  "stop": [
    "stop"
  ],
  "synonyms": [
    "synonyms"
  ],
  "crf": [
    "crf"
  ],
  "isQuantifierRecognition": "true",
  "isRealName": "false",
  "isNumRecognition": "true",
  "isNameRecognition": "true",
  "dic": [
    "dic"
  ]
}
```

# 使用
+ 第一步：创建索引
```text
PUT /test_index?pretty
{
  "settings" : {
    "index" : {
      "number_of_shards" : 16,
      "number_of_replicas" : 1,
      "refresh_interval":"5s"
    }
  },
  "mappings" : {
    "properties" : {
      "test_field": { 
        "type": "text",
        "analyzer": "index_ansj",
        "search_analyzer": "query_ansj"
      }
    }
  }
}
```

> **说明：**
> + `test_index`: 用于测试的索引名称；
> + `test_field`: 用于测试的字段；
> + 指定字段的索引分词器为： `index_ansj` ；
> + 指定字段的搜索分词器为： `query_ansj` ；

测试索引配置是否正确：
```text
POST /test_index/_analyze
{
  "field": "test_field",
  "text": "中国"
}
```

+ 第二步：添加数据  
```text
PUT test_index/_bulk?refresh
{"create":{ }}
{ "test_field" : "中国" }
{"create":{ }}
{ "test_field" : "中华人民共和国" }
{"create":{ }}
{ "test_field" : "中国有56个民族" }
{"create":{ }}
{ "test_field" : "中国是社会主义国家" }
```

+ 第三步：执行搜索  
```text
GET test_index/_search
{
  "query": {
    "match": {
      "test_field": {
        "query": "中国"
      }
    }
  }
}
```

> **注意：**  
> + 上述操作语句都是在 `kibana` 的 `dev_tools` 里执行的；
> + 上述操作语句仅在 es `8.x` 版本上测试过，其它版本请根据实际情况调整。

# 插件功能
安装插件后，在 es 集群中会增加以下功能：

**三个 analyzer:**
+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ dic_ansj

**三个 tokenizer：**
+ index_ansj (建议索引使用)
+ query_ansj (建议搜索使用)
+ dic_ansj

**http 接口：**
+ /_cat/ansj: 执行分词
+ /_cat/ansj/config: 显示全部配置
+ /_ansj/flush/config: 刷新全部配置
+ /_ansj/flush/dic: 更新全部词典。包括用户自定义词典,停用词典,同义词典,歧义词典,crf

# 配置文件
## 配置文件格式
```yaml
ansj:
  #默认参数配置
  isNameRecognition: true #开启姓名识别
  isNumRecognition: true #开启数字识别
  isQuantifierRecognition: true #是否数字和量词合并
  isRealName: false #是否保留真实词语,建议保留false

  #用户自定词典配置
  #dic: default.dic #也可以写成 file://default.dic , 如果未配置dic,则此词典默认加载
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
```

## 配置文件示例
### 使用本地文件词库
```yaml
ansj:
  # 开启姓名识别
  isNameRecognition: false
  # 开启数字识别
  isNumRecognition: true
  # 是否数字和量词合并
  isQuantifierRecognition: false
  # 是否保留真实词语
  isRealName: false
  # 词典
  dic: file:///data/elasticsearch-dic/ansj/main.dic
  # 停词（过滤词）词典
  stop: file:///data/elasticsearch-dic/ansj/stop.dic
  # 歧义词词典配置
  ambiguity: file:///data/elasticsearch-dic/ansj/ambiguity.dic
  # 同义词词典配置
  synonyms: file:///data/elasticsearch-dic/ansj/synonyms.dic
```

### 使用 HTTP 协议加载词库
```yaml
ansj:
  # 开启姓名识别
  isNameRecognition: false
  # 开启数字识别
  isNumRecognition: true
  # 是否数字和量词合并
  isQuantifierRecognition: false
  # 是否保留真实词语
  isRealName: false
  # 词典
  dic: http://example.com/elasticsearch-dic/ansj/main.dic
  # 停词（过滤词）词典
  stop: http://example.com/elasticsearch-dic/ansj/stop.dic
  # 歧义词词典配置
  ambiguity: http://example.com/elasticsearch-dic/ansj/ambiguity.dic
  # 同义词词典配置
  synonyms: http://example.com/elasticsearch-dic/ansj/synonyms.dic
```

# 插件版本与 ES 版本的对应关系

| plugin  |     elasticsearch|
|---------|       -----:  |
| 1.0.0   |     0.90.2    |
| 1.x     |     1.x       |
| 2.1.1   |     2.1.1     |
| 2.3.1   |     2.3.1     |
| 2.3.2   |     2.3.2     |
| 2.3.3   |     2.3.3     |
| 2.3.4   |     2.3.4     |
| 2.3.5   |     2.3.5     |
| 2.4.0   |     2.4.0     |
| 2.4.1   |     2.4.1     |
| 2.4.2   |     2.4.2     |
| 2.4.3   |     2.4.3     |
| 2.4.4   |     2.4.4     |
| 2.4.5   |     2.4.5     |
| 2.4.6   |     2.4.6     |
| 5.0.0   |     5.0.0     |
| 5.0.1   |     5.0.1     |
| 5.0.2   |     5.0.2     |
| 5.1.1   |     5.1.1     |
| 5.1.2   |     5.1.2     |
| 5.2.0   |     5.2.0     |
| 5.2.1   |     5.2.1     |
| 5.2.2   |     5.2.2     |
| 5.3.0   |     5.3.0     |
| 5.3.1   |     5.3.1     |
| 5.3.2   |     5.3.2     |
| 5.3.3   |     5.3.3     |
| 5.4.0   |     5.4.0     |
| 5.4.1   |     5.4.1     |
| 5.4.2   |     5.4.2     |
| 5.4.3   |     5.4.3     |
| 5.5.0   |     5.5.0     |
| 5.5.1   |     5.5.1     |
| 5.5.2   |     5.5.2     |
| 5.5.3   |     5.5.3     |
| 5.6.0   |     5.6.0     |
| 5.6.1   |     5.6.1     |
| 5.6.2   |     5.6.2     |
| 5.6.3   |     5.6.3     |
| 5.6.4   |     5.6.4     |
| 5.6.5   |     5.6.5     |
| 5.6.6   |     5.6.6     |
| 5.6.7   |     5.6.7     |
| 5.6.8   |     5.6.8     |
| 5.6.9   |     5.6.9     |
| 5.6.10  |     5.6.10    |
| 5.6.11  |     5.6.11    |
| 5.6.12  |     5.6.12    |
| 5.6.13  |     5.6.13    |
| 5.6.14  |     5.6.14    |
| 5.6.15  |     5.6.15    |
| 5.6.16  |     5.6.16    |
| 6.0.0   |     6.0.0     |
| 6.0.1   |     6.0.1     |
| 6.1.0   |     6.1.0     |
| 6.1.1   |     6.1.1     |
| 6.1.2   |     6.1.2     |
| 6.1.3   |     6.1.3     |
| 6.1.4   |     6.1.4     |
| 6.2.0   |     6.2.0     |
| 6.2.1   |     6.2.1     |
| 6.2.2   |     6.2.2     |
| 6.2.3   |     6.2.3     |
| 6.2.4   |     6.2.4     |
| 6.3.0   |     6.3.0     |
| 6.3.1   |     6.3.1     |
| 6.3.2   |     6.3.2     |
| 6.4.0   |     6.4.0     |
| 6.4.1   |     6.4.1     |
| 6.4.2   |     6.4.2     |
| 6.4.3   |     6.4.3     |
| 6.5.0   |     6.5.0     |
| 6.5.1   |     6.5.1     |
| 6.5.2   |     6.5.2     |
| 6.5.3   |     6.5.3     |
| 6.5.4   |     6.5.4     |
| 6.6.0   |     6.6.0     |
| 6.6.1   |     6.6.1     |
| 6.6.2   |     6.6.2     |
| 6.7.0   |     6.7.0     |
| 6.7.1   |     6.7.1     |
| 6.7.2   |     6.7.2     |
| 6.8.0   |     6.8.0     |
| 6.8.1   |     6.8.1     |
| 6.8.2   |     6.8.2     |
| 6.8.3   |     6.8.3     |
| 6.8.4   |     6.8.4     |
| 6.8.5   |     6.8.5     |
| 6.8.6   |     6.8.6     |
| 6.8.7   |     6.8.7     |
| 6.8.8   |     6.8.8     |
| 6.8.9   |     6.8.9     |
| 6.8.10  |     6.8.10    |
| 6.8.11  |     6.8.11    |
| 6.8.12  |     6.8.12    |
| 6.8.13  |     6.8.13    |
| 6.8.14  |     6.8.14    |
| 6.8.15  |     6.8.15    |
| 6.8.16  |     6.8.16    |
| 6.8.17  |     6.8.17    |
| 6.8.18  |     6.8.18    |
| 6.8.19  |     6.8.19    |
| 6.8.20  |     6.8.20    |
| 6.8.21  |     6.8.21    |
| 6.8.22  |     6.8.22    |
| 6.8.23  |     6.8.23    |
| 7.0.0   |     7.0.0     |
| 7.0.1   |     7.0.1     |
| 7.1.0   |     7.1.0     |
| 7.1.1   |     7.1.1     |
| 7.2.0   |     7.2.0     |
| 7.2.1   |     7.2.1     |
| 7.3.0   |     7.3.0     |
| 7.3.1   |     7.3.1     |
| 7.3.2   |     7.3.2     |
| 7.4.0   |     7.4.0     |
| 7.4.1   |     7.4.1     |
| 7.4.2   |     7.4.2     |
| 7.5.0   |     7.5.0     |
| 7.5.1   |     7.5.1     |
| 7.5.2   |     7.5.2     |
| 7.6.0   |     7.6.0     |
| 7.6.1   |     7.6.1     |
| 7.6.2   |     7.6.2     |
| 7.7.0   |     7.7.0     |
| 7.7.1   |     7.7.1     |
| 7.8.0   |     7.8.0     |
| 7.8.1   |     7.8.1     |
| 7.9.0   |     7.9.0     |
| 7.9.1   |     7.9.1     |
| 7.9.2   |     7.9.2     |
| 7.9.3   |     7.9.3     |
| 7.17.5  |     7.17.5    |
| 7.17.7  |     7.17.7    |
| 7.17.8  |     7.17.8    |
| 7.17.9  |     7.17.9    |
| 7.17.10 |     7.17.10   |
| 7.17.11 |     7.17.11   |
| 7.17.12 |     7.17.12   |
| 7.17.13 |     7.17.13   |
| 7.17.14 |     7.17.14   |
| 7.17.15 |     7.17.15   |
| 7.17.16 |     7.17.16   |
| 7.17.17 |     7.17.17   |
| 7.17.18 |     7.17.18   |
| 7.17.19 |     7.17.19   |
| 7.17.20 |     7.17.20   |
| 7.17.21 |     7.17.21   |
| 7.17.22 |     7.17.22   |
| 8.3.3   |     8.3.3     |
| 8.5.3   |     8.5.3     |
| 8.6.0   |     8.6.0     |
| 8.6.1   |     8.6.1     |
| 8.6.2   |     8.6.2     |
| 8.7.0   |     8.7.0     |
| 8.7.1   |     8.7.1     |
| 8.8.0   |     8.8.0     |
| 8.8.1   |     8.8.1     |
| 8.8.2   |     8.8.2     |
| 8.9.0   |     8.9.0     |
| 8.9.1   |     8.9.1     |
| 8.9.2   |     8.9.2     |
| 8.10.0  |     8.10.0    |
| 8.10.1  |     8.10.1    |
| 8.10.2  |     8.10.2    |
| 8.10.3  |     8.10.3    |
| 8.10.4  |     8.10.4    |
| 8.11.0  |     8.11.0    |
| 8.11.1  |     8.11.1    |
| 8.11.2  |     8.11.2    |
| 8.11.3  |     8.11.3    |
| 8.11.4  |     8.11.4    |
| 8.12.0  |     8.12.0    |
| 8.12.1  |     8.12.1    |
| 8.12.2  |     8.12.2    |
| 8.13.0  |     8.13.0    |
| 8.13.1  |     8.13.1    |
| 8.13.2  |     8.13.2    |
| 8.13.3  |     8.13.3    |
| 8.13.4  |     8.13.4    |
| 8.14.0  |     8.14.0    |
| 8.14.1  |     8.14.1    |
| 8.14.2  |     8.14.2    |
| 8.14.3  |     8.14.3    |

# 版权
`elasticsearch-analysis-ansj` is licenced under the Apache License Version 2.0. See the [LICENSE](https://github.com/NLPchina/elasticsearch-analysis-ansj/blob/master/LICENSE) file for details.
