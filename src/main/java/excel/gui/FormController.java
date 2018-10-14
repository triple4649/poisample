package excel.gui;

import java.io.File;
import java.util.function.Function;

import excel.FindingStr;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class FormController {

	@FXML private TextField inputpath;
	@FXML private TextField outputpath;
	@FXML private TextField searchStr;
	@FXML private TextArea resultField;
	@FXML private CheckBox checkReg;
	@FXML private CheckBox checkUpLowerStr;
	//セルから値が取れた時に実行するFunctionを指定する
	private Function<String,String> funcOnRegClick = null;
	//セルから値が取れた時に実行するFunctionを指定する
	private Function<String,String> funcOnUpLowStr = s->s;

	
	
	@FXML 
	protected void searchInput(ActionEvent evt) {
		chooseDiectory(this.inputpath);
	}
	@FXML 
	protected void searchOutput(ActionEvent evt) {
		chooseDiectory(this.outputpath);
	}
	//正規表現を使うcheckボックスの押下状況に応じて
	//Grep処理を切り替える
	@FXML 
	protected void onRegCheck(ActionEvent evt) {
		String target =funcOnUpLowStr.apply(searchStr.getText());
		if(this.checkReg.isSelected()) {
			funcOnRegClick=s->{
				if(funcOnUpLowStr.apply(s).matches(target))return s;
				else return "";
			};
		}else {
			funcOnRegClick=s->{
				if(funcOnUpLowStr.apply(s).indexOf(target)>=0)return s;
				else return "";
			};
		}
	}
	
	@FXML 
	//大文字小文字を使うcheckボックスの押下状況に応じて
	//大文字小文字変換を切り替える
	protected void onUpLowerStrcheck(ActionEvent evt) {
		if(this.checkUpLowerStr.isSelected()) {
			funcOnUpLowStr=s->s.toLowerCase();
		}else {
			funcOnUpLowStr=s->s;
		}
	}
	
	@FXML
	protected void onSearch(ActionEvent evt) {
		try {
			new FindingStr(
				//ディレクトリ操作処理実行中にやりたいことをラムダ式で指定する
				s->resultField.setText(s + "\n" +resultField.getText() ),
				//セルの値を取得したときにやりたいことを関数型インターフェイスで指定する
				funcOnRegClick
			).search(inputpath.getText(), 
					outputpath.getText()+"\\result.txt");
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
