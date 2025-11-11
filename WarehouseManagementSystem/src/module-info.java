module warehouse {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	requires javafx.base;
	requires javafx.graphics;
	
	opens main to javafx.graphics, javafx.fxml;
	opens logger to javafx.graphics, javafx.fxml;
	opens application to javafx.graphics, javafx.fxml;
	opens application.inventory_screen to javafx.graphics, javafx.fxml;
	opens application.robot_screen to javafx.graphics, javafx.fxml;

}
