package com.chatroom.client;

import javafx.application.Platform;

/**
 * 各个stage的自定义基类
 * @author ZhangJing
 *
 */
public class ControlledStage {
	//Stage管理器
	protected StageController myController;
	//StageID
	protected String myStageUIID;

	public void setStageController(StageController stageController){
		this.myController = stageController;
	}

	public void setStageName(String stageUIID){
		this.myStageUIID = stageUIID;
	}

	/**
	 * 关闭原界面，切换新界面
	 * @param stage 新界面名称
	 */
	public void changeStage(String stage) {
		// TODO Auto-generated method stub
		Platform.runLater(() ->{
			myController.showStage(stage, myStageUIID);
		});


	}
}
