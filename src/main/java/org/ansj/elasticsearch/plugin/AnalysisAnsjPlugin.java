package org.ansj.elasticsearch.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import love.cq.domain.Forest;
import love.cq.library.Library;
import love.cq.splitWord.GetWord;

import org.ansj.elasticsearch.index.AnsjAnalysisBinderProcessor;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisAnsjPlugin extends AbstractPlugin {

    @Override public String name() {
        return "analysis-ansj";
    }


    @Override public String description() {
        return "ansj analysis";
    }


    @Override public void processModule(Module module) {
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
            analysisModule.addProcessor(new AnsjAnalysisBinderProcessor());
        }
    }
    
    public static void main(String[] args){
    	/**
         * 词典的构造.一行一个词后面是参数.可以从文件读取.可以是read流.
         */
        String dic = "白癜风\n肾功能\n恶心\n腹胀\n腹痛";
        Forest forest=null;
		try {
			forest = Library.makeForest(new BufferedReader(new StringReader(dic)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        /**
         * 删除一个单词
         */
        Library.removeWord(forest, "中国");
        /**
         * 增加一个新词
         */
        Library.insertWord(forest, "中国人");
        String content = "     阿仑膦酸钠片说明书 请仔细阅读说明书并按说明使用或在药师指导下购买和使用 药品名称： 通用名称：阿仑膦酸钠片 英文名称：Alendronate Sodium Tablets 汉语拼音：Alun Linsuanna  Pian 成份：  阿仑膦酸钠 性状：  白色片。 功能主治：  适应于治疗绝经后妇女的骨质疏松症。 规格：  10mg*7片 用法用量：  口服，每日早餐前至少30分钟空腹用200ml温开水送服，一次10mg，一日1次。 不良反应： 服用后耐受性良好，少数病人可见胃肠道反应，如恶心、腹胀、腹痛等，偶有头痛、骨胳肌痉痛等，罕见皮疹或红斑。 禁忌： 1 对本品过敏者禁用。 2 低钙血症患者禁用。 3 孕妇、哺乳期妇女禁用。 注意事项： 1 早餐前至少30分钟用200ml温开水送服，用药后至少30分钟方可进食。 2 与桔子汁和咖啡同时服用会显著地减少本品的吸收。 3 在服用本品前后30分钟内不宜饮用牛奶、奶制品和含较高钙的饮料。 4 服药后即卧床有可能引起食道刺激或溃疡性食管炎。 5 胃肠道紊乱、胃炎、食道不适、十二指肠炎、溃疡病患者慎用。 6 婴幼儿、青少年不宜服用。 7 中、重度肾功能不全患者不宜服用。 8 开始使用本品治疗前，必须治愈钙代谢和矿物质代谢紊乱、维生素D缺乏及低钙血症。 9 补钙剂、抗酸剂和一些口服药剂很可能妨碍本品的吸收，因此，服用本品后应至少推迟半小时再服用其它药物。 10 男性骨质疏松症用药的安全性和有效性尚未验证，不推荐使用。 药物相互作用： 如果同时服用钙补充制剂、抗酸药物和其它口服药物可能会干扰本品吸收。因此，病人在服用本品以后，必须等待至少半小时后，才可服用其他药物。 预计无其它具有临床意义的药物相互作用。 两项为期一或两年的临床研究对绝经后骨质疏松妇女同时应用激素替代治疗（雌激素±孕激素）（静脉同时经皮给药或口服给药）和本品进行了评价。与单独应用相比，联合应用激素替代治疗和本品能更多地增加骨量，更多地降低骨转换。在这些研究中，联合治疗与单独治疗在安全性和可耐受性方面是一致的。 特异性相互作用研究尚未进行。在治疗男性和绝经后妇女的骨质疏松症的研究中，本品已与各种常用处方药同时使用，无明显确定的临床不良相互作用。 贮藏：  密闭保存。 包装：  10mg*7片 有效期：  12个月 批准文号：  国药准字H20093090 生产企业：  成都天台山制药 *如有问题可与生产企业联系 【国家基本药物】 【中药保护品种】 【医保类型】医乙 【物价部门定价】无 【认证情况】通过GMP认证 【委托加工】 【检验合格情况】合格 ---------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------- 温馨提示：由于部分商品包装更换较为频繁，因此您收到的货物有可能与图片不完全一致，请您以收到的商品实物为准，同时我们会尽量做 到及时更新，由此给您带来不便请多多谅解，谢谢！ 纠错信息：如果您发现商品信息不准确，欢迎纠错。 价格举报：如果您发现有比一号药网价格更低的，欢迎举报。";
        GetWord udg = forest.getWord(content);

        String temp = null;
        while ((temp = udg.getFrontWords()) != null)
            System.out.println(temp + "\t\t" + udg.getParam(1) + "\t\t" + udg.getParam(2));
        StringReader reader = new StringReader("无菌敷贴 6*7cm");
        Set<String> filter = new HashSet<String>();
        filter.add(" ");
        filter.add("6");
        filter.add("7");
        filter.add("*");
        Tokenizer tokenizer = new AnsjTokenizer(new ToAnalysis(reader), reader, filter, false);
        try {
			while(tokenizer.incrementToken()){
			    CharTermAttribute attribute = tokenizer.getAttribute(CharTermAttribute.class) ;
			    System.out.println(attribute);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        

    }
}