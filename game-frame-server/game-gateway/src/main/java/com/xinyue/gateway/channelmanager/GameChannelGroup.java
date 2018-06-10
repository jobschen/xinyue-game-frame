package com.xinyue.gateway.channelmanager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;

/**
 * 一个GameChannel的集合，负责管理channel与用户的id的映射。这里使用一个EventExecutor来控制对集合的操作，保证集合所有的操作都在同一个线程中。
 * 
 * @author 心悦网络科技有限公司 王广帅
 *
 * @Date 2018年6月3日 下午9:40:04
 */
public class GameChannelGroup {
	private static Logger logger = LoggerFactory.getLogger(GameChannelGroup.class);
	/**
	 * 缓存用户id和channel的映射
	 */
	private Map<Long, Channel> channelMap = new HashMap<>();
	private EventExecutor executor;

	public GameChannelGroup(EventExecutor executor) {
		this.executor = executor;
	}

	private void execute(Runnable task) {
		this.executor.execute(task);
	}

	public void addChannel(Long userId, Channel channel) {
		this.execute(() -> {
			channelMap.put(userId, channel);
		});
	}

	public void removeChannel(Long userId) {
		this.execute(() -> {
			channelMap.remove(userId);
		});
	}

	public void writeMessage(Long userId, Object message) {
		this.execute(() -> {
			Channel channel = channelMap.get(userId);
			if (channel == null) {
				logger.debug("userId[{}]对应的channel为null", userId);
			} else {
				if (channel.isActive() && channel.isOpen()) {
					channel.writeAndFlush(message);
				} else {
					logger.debug("userId[{}]的channel已关闭", userId);
				}
			}
		});
	}

}