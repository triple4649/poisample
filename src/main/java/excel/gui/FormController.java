package excel.gui;

import java.io.File;

import excel.FindingStr;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class FormController {

	@FXML private TextField inputpath;
	@FXML private TextField outputpath;
	@FXML private TextField searchStr;
	@FXML private TextArea resultField;
	
	@FXML 
	protected void searchInput(ActionEvent evt) {
		chooseDiectory(this.inputpath);
	}
	@FXML 
	protected void searchOutput(ActionEvent evt) {
		chooseDiectory(this.outputpath);
	}
	@FXML
	protected void onSearch(ActionEvent evt) {
		try {
			new FindingStr(
				s->resultField.setText(s + "\n" +resultField.getText() )
			)
			.search(inputpath.getText(), 
					outputpath.getText()+"\\result.txt",
					searchStr.getText());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void chooseDiectory(TextField t) {
		DirectoryChooser  fc = new DirectoryChooser ();
		fc.setTitle("ディレクトリ選択");
		File file = fc.showDialog(null);
		if(file!=null) {
			t.setText(file.getAbsolutePath());
		}
		
	}
}
