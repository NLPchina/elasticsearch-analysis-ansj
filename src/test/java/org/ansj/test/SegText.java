package org.ansj.test;

import java.util.ArrayList;
import java.util.List;

import org.ansj.splitWord.analysis.ToAnalysis;

public class SegText {
	public static void main(String[] args) {
		List<String> all = new ArrayList<>() ;
		
		all.add("六味地黄丸软胶囊") ;
		
		for (String string : all) {
			System.out.println(ToAnalysis.parse(string));
		}
	}
}
