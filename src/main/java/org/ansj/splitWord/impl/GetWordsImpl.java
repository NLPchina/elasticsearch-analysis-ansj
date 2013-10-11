package org.ansj.splitWord.impl;

import static org.ansj.library.InitDictionary.base;
import static org.ansj.library.InitDictionary.check;
import static org.ansj.library.InitDictionary.conversion;
import static org.ansj.library.InitDictionary.status;
import static org.ansj.library.InitDictionary.termNatures;
import static org.ansj.library.InitDictionary.words;

import org.ansj.domain.TermNatures;
import org.ansj.splitWord.GetWords;

public class GetWordsImpl implements GetWords {

	/**
	 * offe : 当前词的偏移量
	 */
	public int offe;

	/**
	 * 构造方法，同时加载词典,传入词语相当于同时调用了setStr() ;
	 */
	public GetWordsImpl(String str) {
		setStr(str);
	}

	/**
	 * 构造方法，同时加载词典
	 */
	public GetWordsImpl() {
	}

	int charsLength = 0;

	@Override
	public void setStr(String chars) {
		this.chars = chars;
		charsLength = chars.length();
		checkValue = 0;
	}

	public String chars;
	private int charHashCode;
	private int start = 0;
	public int end = 0;
	private int baseValue = 0;
	private int checkValue = 0;
	private int tempBaseValue = 0;
	public int i = 0;
	private String str = null;

	@Override
	public String allWords() {
		for (; i < charsLength; i++) {
			charHashCode = conversion(chars.charAt(i));
			end++;
			switch (getStatement()) {
			case 0:
				if (baseValue == chars.charAt(i)) {
					str = String.valueOf(chars.charAt(i));
					offe = i;
					start = ++i;
					end = 0;
					baseValue = 0;
					tempBaseValue = baseValue;
					return str;
				} else {
					i = start;
					start++;
					end = 0;
					baseValue = 0;
					break;
				}
			case 2:
				i++;
				offe = start;
				tempBaseValue = baseValue;
				return words[tempBaseValue];
			case 3:
				offe = start;
				start++;
				i = start;
				end = 0;
				tempBaseValue = baseValue;
				baseValue = 0;
				return words[tempBaseValue];
			}

		}
		if (start++ != i) {
			i = start;
			baseValue = 0;
			return allWords();
		}
		start = 0;
		end = 0;
		baseValue = 0;
		i = 0;
		return null;
	}

	/**
	 * 根据用户传入的c得到单词的状态. 0.代表这个字不在词典中 1.继续 2.是个词但是还可以继续 3.停止已经是个词了
	 * 
	 * @param c
	 * @return
	 */
	private int getStatement() {
		checkValue = baseValue;
		baseValue = base[checkValue] + charHashCode;
		if (check[baseValue] == checkValue || check[baseValue] == -1) {
			return status[baseValue];
		}
		return 0;
	}

	public byte getStatus() {
		// TODO Auto-generated method stub
		return status[tempBaseValue];
	}

	/**
	 * 获得当前词的词性
	 * 
	 * @return
	 */
	public TermNatures getTermNatures() {
		// TODO Auto-generated method stub
		TermNatures tns = termNatures[tempBaseValue];
		if (tns == null) {
			return TermNatures.NULL;
		}
		return tns;
	}

	public static void main(String[] args) {
		GetWords gwi = new GetWordsImpl();
		gwi.setStr("关联商品_1号药网   白蚀丸说明书 请仔细阅读说明书并在医师指导下使用 药品名称： 通用名称：白蚀丸 汉语拼音：Baishi Wan 成份： 紫草、灵芝、降香、补骨脂、丹参、红花、何首乌、海螵蛸、牡丹皮、黄药子、苍术、甘草、蒺藜、龙胆。 性状： 本品为黑色的包衣浓缩水丸，除去包衣后显棕褐色；味苦。 功能主治： 补益肝肾，活血祛瘀，养血驱风。用于治疗白癜风。 规格： 每瓶装30g 用法用量： 口服，一次2.5g，十岁以下小儿服量减半，一日3次。 不良反应： 个别患者用药后可能产生肝功能异常。 禁忌： 孕妇、肝功能不全者禁用。 注意事项： 1.患部宜常日晒。 　　 2.服药期间，出现全身乏力，食欲不振，肝区疼痛或皮肤发黄等肝功能异常者，应立即停药并就医。必要时应做肝功能检查。 　　 3.请按规定用量服用，儿童服用请遵医嘱。哺乳期妇女慎用。 贮藏： 密封，置阴凉干燥处。 包装： 塑瓶包装，每瓶装30克，每盒1瓶。 有效期： 36个月 执行标准： 部颁标准中药成方制剂第十一册WS3-B-2115-96及药典业发（1999）第376号 批准文号： 国药准字Z44020112 生产企业： 广州中一药业有限公司 如有问题可与生产企业联系 【国家基本药物】 【中药保护品种】 【医保类型】 【物价部门定价】无 【认证情况】通过GMP认证 【委托加工】无 【批号】 【检验合格情况】合格 温馨提示： 由于部分商品包装更换较为频繁，因此您收到的货物有可能与图片不完全一致，请您以收到的商品实物为准，同时我们会尽量做到及时更新，由此给您带来不便请多多谅解，谢谢！ 纠错信息：如果您发现商品信息不准确，欢迎纠错。 价格举报：如果您发现有比一号药网价格更低的，欢迎举报。");
		String temp = null;
		while ((temp = gwi.allWords()) != null) {
			System.out.println(temp + gwi.getOffe());
		}
	}

	@Override
	public int getOffe() {
		// TODO Auto-generated method stub
		return offe;
	}

}