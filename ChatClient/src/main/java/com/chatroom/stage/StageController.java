package com.chatroom.stage;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

//参考来源： http://blog.csdn.net/nthack5730/article/details/51901593

/**
 *
 * @Title: StageController.java
 * @Description: TODO stage控制器
 * @author ZhangJing
 * @date 2017年5月31日 下午9:22:48
 *
 */
public class StageController {
	private static final Logger logger = LoggerFactory.getLogger(StageController.class);
	// 用于存放所有stages对象
	private HashMap<String, Stage> stages = new HashMap<String, Stage>();;

	/**
	 * 添加stage到Map中
	 *
	 * @param name
	 * @param stage
	 */
	public void addStage(String name, Stage stage) {
		stages.put(name, stage);
	}

	/**
	 * 将主舞台的对象保存起来，这里只是为了以后可能需要用，目前还不知道用不用得上
	 *
	 * @param primaryStageName
	 *            设置主舞台的名称
	 * @param primaryStage
	 *            主舞台对象，在Start()方法中由JavaFx的API建立
	 */
	public void addPrimaryStage(Stage primaryStage) {
		this.addStage("primaryStage", primaryStage);
	}

	public Stage getPrimaryStage() {
		return stages.get("primaryStage");
	}

	/**
	 * 通过stage的名字得到stage对象
	 *
	 * @param name
	 * @return
	 */
	public Stage getStage(String name) {
		return stages.get(name);
	}

	/**
	 * 移除Map中相应的stage
	 *
	 * @param name
	 */
	/*
	 * public boolean rvStage(String name){ if(stages.remove(name) != null){
	 * return true; } else { return false; } }
	 */

	/**
	 * 加载stage的Fxml文件并将stage对象存贮到Map中
	 *
	 * @param name
	 *            注册好的fxml窗口的文件
	 * @param resources
	 *            fxml资源地址
	 * @param isModality
	 *            窗口是否需要模式化
	 * @param styles
	 *            可变参数，init使用的初始化样式资源设置
	 * @return 是否加载成功
	 */

	public boolean loadStage(String name, String resourse, boolean isModality, StageStyle... styles) {
		try {
			// 加载FXML文件
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(resourse));
			// FXMLLoader loader = new
			// FXMLLoader(getClass().getResource(resourse));
			Parent root;
			root = (Parent) loader.load();

			// 构造对应的stage
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setScene(scene);
			// 设置模式
			if (isModality) {
				stage.initModality(Modality.APPLICATION_MODAL);
			}

			// 通过Loader获取FXML对应的ViewCtr，并将本StageController注入到ViewCtr中

			ControlledStage controlledStage = (ControlledStage) loader.getController();
			controlledStage.setStageController(this);
			controlledStage.setStageName(name);
			// 配置initStyle
			for (StageStyle style : styles) {
				stage.initStyle(style);
			}

			logger.debug("{} is creating..", name);

			// 添加到Map中
			this.addStage(name, stage);

			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 在Map中删除Stage加载对象
	 *
	 * @param name
	 *            需要删除的fxml窗口文件名
	 * @return 是否删除成功
	 */
	public boolean unloadStage(String name) {
		if (stages.remove(name) == null) {
			logger.debug("{} is not existing!", name);
			return false;
		} else {
			logger.debug("{} is destroying..", name);
			return true;
		}
	}

	/**
	 * 显示Stage但不隐藏任何Stage
	 *
	 * @param name
	 *            需要显示的窗口的名称
	 * @return 是否显示成功
	 */
	public boolean showStage(String name) {
		this.getStage(name).show();
		logger.debug("{} is showing..", name);
		return true;
	}

	/**
	 * 显示Stage并隐藏对应的窗口
	 *
	 * @param show
	 *            需要显示的窗口
	 * @param close
	 *            需要关闭的窗口
	 * @return
	 */
	public boolean showStage(String show, String close) {
		closeStage(close);
		this.getStage(show).show();
		logger.debug("{} is showing..", show);
		return true;
	}

	/**
	 * 关闭Stage
	 *
	 * @param name
	 *            需要关闭的窗口
	 * @return
	 */
	public boolean closeStage(String name) {
		getStage(name).close();
		logger.debug("{} is closing..", name);
		return true;
	}

	/**
	 * 检查窗口是否打开
	 *
	 * @param name
	 *            窗口名称
	 * @return
	 */
	public boolean isShowingStage(String name) {
		if (getStage(name).isShowing()) {
			return true;
		} else {
			return false;
		}
	}

}
