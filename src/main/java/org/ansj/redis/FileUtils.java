package org.ansj.redis;

import static org.elasticsearch.index.analysis.AnsjStaticValue.environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

public class FileUtils {

	public static ESLogger logger = Loggers.getLogger("ansj-redis-msg-file");
	
	public static void remove(String content){
		try {
			File file = new File(environment.configFile(),"ansj/user/ext.dic");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<String> list = new ArrayList<String>();
	        String text = reader.readLine();
	        while (text != null) {
	            if (!text.trim().equals(content)) {
	                list.add(text + "\r\n");
	            }
	            text = reader.readLine();
	        }
	        reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(String item:list){
				writer.write(item);
				writer.newLine();
			}
			writer.close();
			
		} catch (FileNotFoundException e) {
			logger.error("file not found $ES_HOME/config/ansj/user/ext.dic", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("read exception", e);
			e.printStackTrace();
		}
		
	}
	
	public static void append(String content){
		try {
			File file = new File(environment.configFile(),"ansj/user/ext.dic");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
			writer.write(content);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			logger.error("read exception", e);
			e.printStackTrace();
		}
	}
	
	
	
}
