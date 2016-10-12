package org.ansj.elasticsearch.entities;

import org.elasticsearch.common.settings.Settings;

public class RedisStatusEntity {
	private static Settings essettings;
	private static boolean redischeckdeamonalived = false;
	private static long redisthreadid;

	public static Settings getEssettings() {
		return essettings;
	}

	public static void setEssettings(Settings essettings) {
		RedisStatusEntity.essettings = essettings;
	}

	public static boolean isRedischeckdeamonalived() {
		return redischeckdeamonalived;
	}

	public static void setRedischeckdeamonalived(boolean redischeckdeamonalived) {
		RedisStatusEntity.redischeckdeamonalived = redischeckdeamonalived;
	}

	public static long getRedisthreadid() {
		return redisthreadid;
	}

	public static void setRedisthreadid(long redisthreadid) {
		RedisStatusEntity.redisthreadid = redisthreadid;
	}

}
