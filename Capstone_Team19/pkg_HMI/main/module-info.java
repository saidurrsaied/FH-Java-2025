module warehouse {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	
	opens main to javafx.graphics, javafx.fxml;
	opens logging to javafx.graphics, javafx.fxml;
	opens application to javafx.graphics, javafx.fxml;

}
