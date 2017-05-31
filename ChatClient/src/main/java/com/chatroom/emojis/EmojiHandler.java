package com.chatroom.emojis;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Emoji管理器:
 * ①导入emoji实体，
 * ②管理emoji各类集合，
 * ③转化不同的emoji表现形式(简称、unicode、hex)，
 * ④解析分离字符串中的emoji和文本
 *
 * @Title: EmojiHandler.java
 * @Description: TODO
 * @author ZhangJing
 * @date 2017年5月29日 下午10:10:33
 * 参考：https://github.com/UltimateZero/EmojiOneJava
 */
public class EmojiHandler {
	private static final String path = "./src/main/resources/emoji/emoji.json";
	private static final Logger logger = LoggerFactory.getLogger(EmojiHandler.class);
	private static EmojiHandler instance = new EmojiHandler();

	private  Map<String, Emoji> emojiMap;
	private  HashMap<String, String> shortnameToUnicode = new HashMap<>();
	private  HashMap<String, String> unicodeToShortname = new HashMap<>();
	private  HashMap<String, String> shortameToHex = new HashMap<>();

	Gson gson = new Gson();
	// 正则匹配模式
	private  Pattern UNICODE_PATTERN;
	private  Pattern SHORTNAME_PATTERN;

	private EmojiHandler() {
		loadEmoji();
	}

	// 单例
	public static EmojiHandler getInstance() {
		return instance;
	}

	public Map<String, Emoji> getEmojiMap() {
		return emojiMap;
	}

	/**
	 * 导入emoji表情，并初始化emoji管理器
	 */
	private void loadEmoji() {

		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(path));
			emojiMap = gson.fromJson(reader, new TypeToken<Map<String, Emoji>>() {
			}.getType());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("emoji.json文件导入有误！请检查文件地址。");
		}

		emojiMap.forEach((name, entry) -> {

			String shortname = entry.getShortname();
			// json文本中的unicode是hex形式的
			String hex = entry.getUnicode();
			String unicode = null;
			if (hex != null && !hex.isEmpty()) {
				unicode = convert(hex);
				// 重新设置Emoji对象中的unicode和hex
				entry.setUnicode(unicode);
				entry.setHex(hex);
			}
			//logger.debug("{}", entry);
			if (shortname == null || shortname.isEmpty() || unicode == null || unicode.isEmpty()) {
				return;
			}
			shortnameToUnicode.put(shortname, unicode);
			unicodeToShortname.put(unicode, shortname);
			shortameToHex.put(unicode, hex);
		});

		// 使用分隔符|(正则表达式中或的意思)将各自的集合元素连成字符串，用于模式匹配
		SHORTNAME_PATTERN = Pattern
				.compile(String.join("|", shortnameToUnicode.keySet().stream().collect(Collectors.toList())));
		UNICODE_PATTERN = Pattern.compile(String.join("|",
				unicodeToShortname.keySet().stream().map(u -> Pattern.quote(u)).collect(Collectors.toList())));
	}

	/**
	 * 根据关键字搜索出对应简称的emoji集合
	 * @param keywords
	 * @return
	 */
	public List<Emoji> search(String keywords) {

		return emojiMap.values().stream()
				.filter(emoji -> emoji.getShortname().contains(keywords))
				.collect(Collectors.toList());

	}

	/**
	 * 将字符串中的emoji与文字解析分离(emoji以简称形式表现)
	 *
	 * @param input 字符串
	 * @return queue 队列存储分离好的emoji和文字
	 */
	public Queue<Object> toEmojiAndText(String input) {
		Queue<Object> queue = new LinkedList<>();
		Matcher matcher = SHORTNAME_PATTERN.matcher(input);
		int lastEnd = 0;
		while (matcher.find()) {
			String lastText = input.substring(lastEnd, matcher.start());
			if (!lastText.isEmpty())
				queue.add(lastText);
			String m = matcher.group();
			emojiMap.values().stream().filter(entry -> entry.getShortname().equals(m)).forEach(entry -> {
				if (entry.getHex() != null && !entry.getHex().isEmpty()) {
					queue.add(entry);
				}
			});
			lastEnd = matcher.end();
		}
		String lastText = input.substring(lastEnd);
		if (!lastText.isEmpty())
			queue.add(lastText);
		return queue;
	}

	/**
	 *	字符串中表情为emoji简称形式的转化成Unicode形式的emoji
	 * @param str
	 * @return
	 */
	public String shortnameToUnicode(String str) {
		String output = replaceWithFunction(str, SHORTNAME_PATTERN, (shortname) -> {
			// 函数式接口实现
			if (shortname == null || shortname.isEmpty() || (!shortnameToUnicode.containsKey(shortname))) {
				return shortname;
			}
			if (shortnameToUnicode.get(shortname).isEmpty()) {
				return shortname;
			}
			String unicode = shortnameToUnicode.get(shortname).toUpperCase();
			return convert(unicode);
		});

		return output;
	}

	/**
	 * 字符串中表情为unicode形式的转化成简称形式的emoji
	 * @param str
	 * @return
	 */
	public String unicodeToShortname(String str) {
		String output = replaceWithFunction(str, UNICODE_PATTERN, (unicode) -> {
			if (unicode == null || unicode.isEmpty() || (!unicodeToShortname.containsKey(unicode))) {
				return unicode;
			}
			return unicodeToShortname.get(unicode);
		});
		return output;
	}

	/**
	 * 在字符串转化不同形式的emoji
	 *
	 * @param input
	 * @param pattern
	 * @param func
	 * @return
	 */
	private String replaceWithFunction(String input, Pattern pattern, Function<String, String> func) {
		StringBuilder builder = new StringBuilder();
		Matcher matcher = pattern.matcher(input);
		int lastEnd = 0;
		// find返回false则不分割
		while (matcher.find()) {
			// 分割并保留文本而不是emoji码
			String lastText = input.substring(lastEnd, matcher.start());
			// String lastText = matcher.group();
			builder.append(lastText);
			// 分割并保留emoji码,再讲不同形式的emoji转化成Unicode
			builder.append(func.apply(matcher.group()));
			lastEnd = matcher.end();
		}
		builder.append(input.substring(lastEnd));
		return builder.toString();
	}

	/**
	 * emoji的字节转换，即hex转换成unicode字节形式
	 *
	 * @param unicodeStr
	 * @return
	 */
	private String convert(String unicodeStr) {
		if (unicodeStr.isEmpty())
			return unicodeStr;
		String[] parts = unicodeStr.split("-");
		StringBuilder buff = new StringBuilder();
		for (String s : parts) {
			int part = Integer.parseInt(s, 16);
			if (part >= 0x10000 && part <= 0x10FFFF) {
				int hi = (int) (Math.floor((part - 0x10000) / 0x400) + 0xD800);
				int lo = ((part - 0x10000) % 0x400) + 0xDC00;
				buff.append(new String(Character.toChars(hi)) + new String(Character.toChars(lo)));
			} else {
				buff.append(new String(Character.toChars(part)));
			}
		}
		return buff.toString();
	}

}
