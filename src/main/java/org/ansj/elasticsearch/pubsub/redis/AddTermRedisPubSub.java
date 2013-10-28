package org.ansj.elasticsearch.pubsub.redis;

import love.cq.domain.Value;
import love.cq.library.Library;

import org.ansj.library.UserDefineLibrary;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import redis.clients.jedis.JedisPubSub;

public class AddTermRedisPubSub extends JedisPubSub {

	public static ESLogger logger = Loggers.getLogger("ansj-redis-msg");

	@Override
	public void onMessage(String channel, String message) {
		logger.debug("channel:" + channel + " and message:" + message,
				new Object[0]);
		String[] msg = message.split(":");
		if (msg.length != 3) {
			return;
		}
		if ("u".equals(msg[0])) {
			if ("c".equals(msg[1])) {
				UserDefineLibrary.insertWord(msg[2], "userDefine", 1000);
				FileUtils.append(msg[2]);
			} else if ("d".equals(msg[1])) {
				UserDefineLibrary.removeWord(msg[2]);
				FileUtils.remove(msg[2]);
			}
		} else if ("a".equals(msg[0]))
			if ("c".equals(msg[1])) {
				String[] cmd = msg[2].split("-");
				Value value = new Value(cmd[0], cmd[1].split(","));
				Library.insertWord(UserDefineLibrary.ambiguityForest, value);
				FileUtils.appendAMB(msg[2].replace(",", "\t").replaceAll("-",
						"\t"));
			} else if ("d".equals(msg[1])) {
				Library.removeWord(UserDefineLibrary.ambiguityForest, msg[2]);
				FileUtils.removeAMB(msg[2]);
			}
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		logger.debug("pattern:" + pattern + " and channel:" + channel
				+ " and message:" + message);
		onMessage(channel, message);
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.info("psubscribe pattern:" + pattern
				+ " and subscribedChannels:" + subscribedChannels);

	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.info("punsubscribe pattern:" + pattern
				+ " and subscribedChannels:" + subscribedChannels);

	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		logger.info("subscribe channel:" + channel + " and subscribedChannels:"
				+ subscribedChannels);

	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		logger.info("unsubscribe channel:" + channel
				+ " and subscribedChannels:" + subscribedChannels);
	}

}
