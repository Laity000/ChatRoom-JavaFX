package com.chatroom.emojis;

import com.google.gson.annotations.SerializedName;
/**
 *
 * @Title: Emoji.java
 * @Description: TODO Emoji实体，用于保存emoji.json导入的属性
 * @author ZhangJing
 * @date 2017年5月29日 下午9:52:39
 *
 */
public class Emoji {

	//emoji简称
	private String shortname;
	//emoji的unicode码(需要转化)
	private String unicode;
	//emoji的hex值(unicode的十六进制表示，用于图片标识)
	private String hex;
	//emoji的序号
	@SerializedName("emoji_order")
	private int emojiOrder;
	//emoji的分类
	private String category;
	//emoji的关键字
	//private String[] keywords;


	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getUnicode() {
		return unicode;
	}
	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}
	public String getHex() {
		return hex;
	}
	public void setHex(String hex) {
		this.hex = hex;
	}

	public int getEmojiOrder() {
		return emojiOrder;
	}
	public void setEmojiOrder(int emojiOrder) {
		this.emojiOrder = emojiOrder;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	/*
	public String[] getKeywords() {
		return keywords;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	*/
	@Override
	public String toString() {
		return "Emoji: [shortname: " +  shortname + ", unicode: " + unicode + ", hex: " + hex +
				", emojiOrder: " +emojiOrder + ", category: " + category + "]";
	}


}
