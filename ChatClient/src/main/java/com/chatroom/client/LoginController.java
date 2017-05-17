package com.chatroom.client;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 *
 * @Title: LoginController.java
 * @Description: TODO 登录窗口控制器
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:19:25
 *
 */
public class LoginController extends ControlledStage implements  Initializable{
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	//Stage管理器
	//private StageController myController;
	//loginController对象
	private static LoginController instance;

	@FXML private BorderPane borderPane;
	@FXML private ImageView defaultImgView;
    @FXML private ImageView randomImgView;
	@FXML private TextField hostnameTextfield;
    @FXML private TextField portTextfield;
    @FXML private TextField usernameTextfield;
    @FXML private Text resultText;

    private double xOffset;
    private double yOffset;

    public final static String[] NameList =
    	{"Alex", "Brenda", "Connie", "Donny",
         "Lynne", "Myrtle", "Rose", "Tony",
         "Williams", "Zach"};


    //注意这里不是单例的形式，UI的Controller相当于回调函数的管理集合。
    //不能用private单例调用，应该由系统调用。这里是为了获得LoginController对象。
    public LoginController() {
		// TODO Auto-generated constructor stub
    	instance = this;
	}
    /**
     * 获得UI的Controller对象
     * @return
     */
    public static LoginController getInstance(){
    	return instance;
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		/* Drag and Drop */
        borderPane.setOnMousePressed(event -> {
            xOffset = myController.getStage(myStageUIID).getX() - event.getScreenX();
            yOffset = myController.getStage(myStageUIID).getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });
        borderPane.setOnMouseDragged(event -> {
            myController.getStage(myStageUIID).setX(event.getScreenX() + xOffset);
            myController.getStage(myStageUIID).setY(event.getScreenY() + yOffset);
        });
        borderPane.setOnMouseReleased(event -> {
            borderPane.setCursor(Cursor.DEFAULT);
        });
	}

	/**
	 * 随机按钮事件：用于随意产生用户名和头像
	 * @param event
	 */
	@FXML public void randomBtnAction(ActionEvent event){
		//清空结果提示
		resultText.setText("");

		int num = new Random().nextInt(10);
		usernameTextfield.setText(NameList[num]);

        defaultImgView.setVisible(false);
        randomImgView.setVisible(true);
        // load the image
        //注意相对路径问题，展示将图片放在src文件夹下
        Image image = new Image("images/" + NameList[num] + ".png");
        // simple displays ImageView the image as is
        // randomImgView.setImage(image);
        randomImgView.setImage(image);

	}

	@FXML public void connectBtnAction(ActionEvent event){
		String username = usernameTextfield.getText().trim();
		String hostname = hostnameTextfield.getText().trim();
		int port = Integer.parseInt(portTextfield.getText().trim());

		//判断用户名不为空
		if("".equals(username)){
			setResultText("Username cannot be empty !");
			return;
		}

		//对头像进行判断
		boolean isDefaultPic = true;
		for(int i= 0; i<10; i++){
			if(username.compareTo(NameList[i]) == 0){
				isDefaultPic = false;
				break;
			}
		}
		String userpic = (isDefaultPic == true) ? "Default.png" : username + ".png";


		ClientThread clientThread = new ClientThread(hostname, port, username, userpic);
		new Thread(clientThread).start();


	}

	/**
	 * 最小化窗口
	 * @param event
	 */
	@FXML public void minBtnAction(ActionEvent event){
		myController.getStage(myStageUIID).setIconified(true);

	}
	/**
	 * 关闭窗口，关闭程序
	 * @param event
	 */
	@FXML public void closeBtnAction(ActionEvent event){
		Platform.exit();
        System.exit(0);

	}

	//--------------对Platform.runLater的理解：------------------
	//①用于更新UI线程中的控件(？有些控件不需要)
	//②JavaFX应用程序的UI线程是不安全的，所以需要其保持顺序执行
	//③考虑的因素：地点：在非UI的线程中更新（在 controller中不需要，如果没有创建 threads in controller)
	//时间：并不能确定更新发生的时间，取决于待更新的数量。如果在ui线程中需要更新的数量多，则需要花一定的时间
	/**
	 * 在LoginUI中显示登陆的结果信息
	 * @param result
	 */
	public void setResultText(String result){
		//此处不需要Platform.runLater的控件
		resultText.setText(result);
		/*
		Platform.runLater(() -> {
		Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning!");
        alert.setHeaderText(result);
        alert.setContentText(result);
        alert.showAndWait();
		});
		*/
	}

}
