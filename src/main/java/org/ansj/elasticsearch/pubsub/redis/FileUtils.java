package org.ansj.elasticsearch.pubsub.redis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.regex.Pattern;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

public class FileUtils {

	public static ESLogger logger = Loggers.getLogger("ansj-redis-msg-file");

	public static void remove(String content) {
		try {
			removeFile(content, AnsjElasticConfigurator.REDIS_LIB_FILE, false);
		} catch (Exception e) {
			logger.error("remove exception", e);
		}
	}

	public static void append(String content) {
		try {
			appendFile(content, AnsjElasticConfigurator.REDIS_LIB_FILE);
		} catch (Exception e) {
			logger.error("append exception", e);
		}
	}

	public static void removeAMB(String content) {
		try {
			File file = new File(AnsjElasticConfigurator.DEFAULT_AMB_FILE_LIB_PATH);
			removeFile(content, file, true);
		} catch (Exception e) {
			logger.error("removaAMB exception", e);
		}
	}

	public static void appendAMB(String content) {
		try {
			File file = new File(AnsjElasticConfigurator.DEFAULT_AMB_FILE_LIB_PATH);
			appendFile(content, file);
		} catch (Exception e) {
			logger.error("appendAMB exception", e);
		}
	}

	private static void appendFile(final String content, final File file) throws Exception {
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(new SpecialPermission());
		}
		AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
			@Override
			public Object run() {
				try (FileWriter fw = new FileWriter(file, true)) {
					BufferedWriter writer = new BufferedWriter(fw);
					writer.write(content);
					writer.newLine();
				} catch (IOException e) {
					logger.error("appendFile exception", e);
				}
				return null;
			}
		});
	}

	private static void removeFile(final String content, final File file, final boolean head) throws Exception {
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(new SpecialPermission());
		}
		AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
			@Override
			public Object run(){
				try (FileReader fr = new FileReader(file); FileWriter fw = new FileWriter(file)) {
					BufferedReader reader = new BufferedReader(fr);
					BufferedWriter writer = new BufferedWriter(fw);
					String text;
					while ((text = reader.readLine()) != null) {
						if (match(content, text, head)) {
							writer.write(text);
							writer.newLine();
						}
					}
				}catch(IOException e){
					logger.error("removeFile exception", e);
				}
				return null;
			}
		});
	}

	private static boolean match(String content, String text, boolean head) {
		if (head)
			return !text.trim().matches("^" + content + "\\D*$");
		return !text.trim().equals(content);
	}

	public static void main(String[] args) {
		Pattern p = Pattern.compile("^满意\\D*$");
		System.out.println(p.matcher("满意  满      a       意      a").matches());
		System.out.println(p.matcher("满哈-满,意").matches());
		System.out.println("满哈-满,意".replace(",", "\t"));
	}

}
