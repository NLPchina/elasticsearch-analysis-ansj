package org.ansj.test;


public class ElasticSearchTest {
    public static void main(String[] args) {

        System.out.println("+ - && || ! ( ) { } [ ] ^ \" ~ * ?".replaceAll("(\\+|-|&|\\||!|\\^|\"|~|\\*|\\?|\\[|\\]|\\(|\\)|\\{|\\})", "\\\\\\\\$1"));
//        //构建一个内存型索引
//        MemoryIndex<String> mi = new MemoryIndex<String>();
//
//        //增加新词
//        String temp = "中国" ;
//
//        //生成各需要建立索引的字段
//        String quanpin = mi.str2QP(temp) ; //zhongguo
//        String jianpinpin = new String(Pinyin.str2FirstCharArr(temp)) ; //zg
//
//        //增加到索引中
//        mi.addItem(temp, temp ,quanpin,jianpinpin);
//
//        temp = "上海" ;
//
//        //生成各需要建立索引的字段
//        quanpin = mi.str2QP(temp) ; //zhongguo
//        jianpinpin = new String(Pinyin.str2FirstCharArr(temp)) ; //zg
//
//        //增加到索引中
//        mi.addItem(temp,1.0, temp ,quanpin,jianpinpin);
//
//        temp = "伤害" ;
//
//        //生成各需要建立索引的字段
//        quanpin = mi.str2QP(temp) ; //zhongguo
//        jianpinpin = new String(Pinyin.str2FirstCharArr(temp)) ; //zg
//
//        //增加到索引中
//        mi.addItem(temp,100.0 ,temp ,quanpin,jianpinpin);
//
//        //搜索提示
//        System.out.println(mi.suggest("zg"));
//        System.out.println(mi.suggest("zhongguo"));
//        System.out.println(mi.suggest("中国"));
//        System.out.println(mi.smartSuggest("中过"));
//        System.out.println(mi.smartSuggest("种过"));
//
//        System.out.println(mi.smartSuggest("shang"));
//        System.out.println(mi.smartSuggest("伤害"));
//        System.out.println(mi.smartSuggest("上海"));
    }
}
