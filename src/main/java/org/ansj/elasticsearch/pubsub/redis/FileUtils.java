package org.ansj.elasticsearch.pubsub.redis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.nlpcn.commons.lang.util.StringUtil;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
			removeFile(content, AnsjElasticConfigurator.AMB_LIB_FILE, true);
		} catch (Exception e) {
			logger.error("removaAMB exception", e);
		}
	}

	public static void appendAMB(String content) {
		try {
			appendFile(content, AnsjElasticConfigurator.AMB_LIB_FILE);
		} catch (Exception e) {
			logger.error("appendAMB exception", e);
		}
	}
	
	/**
	 * 
	 * @Description: 同义词文件中写入内容
	 * @author yeyuelong
	 * @version 2016年9月20日 上午11:14:23 
	 * @param content
	 *
	 */
	public static void appendSYN(String content) {
		try {
			appendFile(content, AnsjElasticConfigurator.SYN_LIB_FILE);
		} catch (Exception e) {
			logger.error("appendSYN exception", e);
		}
	}
	
	/**
	 * 
	 * @Description: 同义词文件中删除内容
	 * @author yeyuelong
	 * @version 2016年9月20日 上午11:19:12 
	 * @param content
	 *
	 */
	public static void removeSYN(String content) {
		try {
			removeFile(content, AnsjElasticConfigurator.SYN_LIB_FILE, true);
		} catch (Exception e) {
			logger.error("removaSYN exception", e);
		}
	}

	private static void appendFile(final String content, final File file) throws Exception {
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(new SpecialPermission());
		}
		AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
			@Override
			public Void run() {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
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

        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws IOException {
                List<String> list = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String text;
                    while ((text = reader.readLine()) != null) {
                        if (match(content, text, head)) {
                            list.add(text);
                        }
                    }
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String item : list) {
                        writer.write(item);
                        writer.newLine();
                    }
                }

                return null;
            }
        });
    }

    private static boolean match(String content, String text, boolean head) {
        if (StringUtil.isNotBlank(text)) {
            text = StringUtil.trim(text);
            String[] split = text.split("\t");
            if (split.length % 2 == 0) {
                // 如果是歧义词
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < split.length; i += 2) {
                    sb.append(split[i]);
                }
                text = sb.toString();
            }
        }
        return head ? !text.trim().matches("^" + content + "\\D*$") : !text.trim().equals(content);
    }

	public static void main(String[] args) {
		Pattern p = Pattern.compile("^满意\\D*$");
		System.out.println(p.matcher("满意  满      a       意      a").matches());
		System.out.println(p.matcher("满哈-满,意").matches());
		System.out.println("满哈-满,意".replace(",", "\t"));
	}

}
