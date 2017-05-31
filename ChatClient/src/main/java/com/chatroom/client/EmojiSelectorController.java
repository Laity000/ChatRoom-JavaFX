package com.chatroom.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.chatroom.emojis.Emoji;
import com.chatroom.emojis.EmojiHandler;
import com.chatroom.emojis.EmojiDisplayer;
import com.chatroom.stage.ControlledStage;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @Title: EmojiSelectorController.java
 * @Description: TODO Emoji选择器
 * @author ZhangJing
 * @date 2017年5月31日 下午9:21:16
 *
 */
public class EmojiSelectorController extends ControlledStage implements Initializable {
	@FXML
	private ScrollPane showScrollPane;
	@FXML
	private FlowPane showFlowPane;
	@FXML
	private TextField searchTextField;
	@FXML
	private ScrollPane searchScrollPane;
	@FXML
	private FlowPane searchFlowPane;

	// EmojiSelectorController对象
	private static EmojiSelectorController instance;

	private ChatController chatController = ChatController.getInstance();

	public EmojiSelectorController() {
		instance = this;
	}

	public static EmojiSelectorController getInstance() {
		return instance;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		//设置图标
        setIcon("images/icon_emoji.png");
		// 设置emoji显示界面
		showScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		showFlowPane.setHgap(5);
		showFlowPane.setVgap(5);
		// 设置搜索结果界面
		searchScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		searchFlowPane.setHgap(5);
		searchFlowPane.setVgap(5);
		// 搜索表情文本框监视器
		searchTextField.textProperty().addListener(x -> {
			String text = searchTextField.getText();
			if (text.isEmpty() || text.length() < 2) {
				searchFlowPane.getChildren().clear();
				searchScrollPane.setVisible(false);
				showScrollPane.setVisible(true);
			} else {
				showScrollPane.setVisible(false);
				searchScrollPane.setVisible(true);
				List<Emoji> results = EmojiHandler.getInstance().search(text);
				searchFlowPane.getChildren().clear();
				results.forEach(emoji -> searchFlowPane.getChildren().add(addEmojiNodeListener(emoji)));
			}
		});
		// 初始化
		init();
	}

	/**
	 * 关闭
	 * @throws IOException
	 */
	@FXML public void closeImgViewPressedAction() {
		closeLocalStage();
	}

	/**
	 * 初始化刷新emoji的显示
	 */
	private void init() {
		Platform.runLater(() -> {
			showFlowPane.getChildren().clear();
			EmojiHandler.getInstance().getEmojiMap().values()
					.forEach(emoji -> showFlowPane.getChildren().add(addEmojiNodeListener(emoji)));
			showScrollPane.requestFocus();
		});
	}

	/**
	 * 创建emoji节点stackpane，并给其添加事件监听器
	 * @param emoji
	 * @return
	 */
	private Node addEmojiNodeListener(Emoji emoji) {
		// 是否需要光标设置
		Node stackPane = EmojiDisplayer.createEmojiNode(emoji, 32, 3);
		if (stackPane instanceof StackPane) {
			// 设置光标手势
			stackPane.setCursor(Cursor.HAND);
			ScaleTransition st = new ScaleTransition(Duration.millis(90), stackPane);
			// 设置提示
			Tooltip tooltip = new Tooltip(emoji.getShortname());
			Tooltip.install(stackPane, tooltip);
			// 设置光标的触发事件
			stackPane.setOnMouseEntered(e -> {
				// stackPane.setStyle("-fx-background-color: #a6a6a6;
				// -fx-background-radius: 3;");
				stackPane.setEffect(new DropShadow());
				st.setToX(1.2);
				st.setToY(1.2);
				st.playFromStart();
				if (searchTextField.getText().isEmpty())
					searchTextField.setPromptText(emoji.getShortname());
			});
			// 设置光标的离开事件
			stackPane.setOnMouseExited(e -> {
				// stackPane.setStyle("");
				stackPane.setEffect(null);
				st.setToX(1.);
				st.setToY(1.);
				st.playFromStart();
			});
			// 设置光标的点击事件
			stackPane.setOnMouseClicked(e -> {
				// 获得emoji简称
				String shortname = emoji.getShortname();
				chatController.getMessageBoxTextArea().appendText(shortname);
				// 关闭emoji选择器
				if (getLocalStage().isShowing()) {
					closeLocalStage();
				}
			});
		}
		return stackPane;
	}

}
