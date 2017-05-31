package com.chatroom.stage;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 各个stage的自定义基类
 * @author ZhangJing
 *
 */
public class ControlledStage {
	//Stage管理器
	private StageController myController;
	//StageID
	private String myStageUIID;


	public void setStageController(StageController stageController){
		this.myController = stageController;
	}

	public void setStageName(String stageUIID){
		this.myStageUIID = stageUIID;
	}
	/**
	 * 得到自身stage对象
	 * @return
	 */
	public Stage getLocalStage(){
		return myController.getStage(myStageUIID);
	}

	/**
	 * 通过stage名称得到相应的stage对象
	 * @param stage 窗口名称
	 * @return
	 */
	public Stage getStage(String stage){
		return myController.getStage(stage);
	}
	/**
	 * 关闭原窗口，切换新窗口
	 * @param stage 新窗口名称
	 */
	public void changeStage(String stage) {
		// TODO Auto-generated method stub
		Platform.runLater(() ->{
			myController.showStage(stage, myStageUIID);
		});
	}
	/**
	 * 打开新窗口
	 * @param stage 新窗口名称
	 */
	public void openStage(String stage) {
		Platform.runLater(() ->{
			myController.showStage(stage);
		});

	}
	/**
	 * 关闭窗口
	 * @param stage 窗口名称
	 */
	public void closeStage(String stage) {
		Platform.runLater(() ->{
			myController.closeStage(stage);
		});
	}

	/**
	 * 关闭自身窗口
	 */
	public void closeLocalStage(){
		Platform.runLater(() ->{
			myController.closeStage(myStageUIID);
		});
	}


	/**
	 * 设置任意窗口位置
	 * @param stage 被选中的窗口名
	 * @param X
	 * @param Y
	 */
	public void setStagePos(String stage, double X, double Y) {
		myController.getStage(stage).setX(X);
		myController.getStage(stage).setY(Y);
	}
	/**
	 * 设置图标
	 * @param path
	 */
	public void setIcon(String path){
		Platform.runLater(() ->{
		myController.getStage(myStageUIID).getIcons().add(new Image(path));
		});
	}


}
