package poi.gui;

import java.io.File;
import java.util.function.Function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import poi.Grep.FindingStr;

public class FormController {

	@FXML private TextField inputpath;
	@FXML private TextField outputpath;
	@FXML private TextField searchStr;
	@FXML private TextArea resultField;
	@FXML private CheckBox checkReg;
	@FXML private CheckBox checkUpLowerStr;
	
	//大文字小文字変換処理を切りかえるFunctionを指定する
	private Function<String,String> funcOnUpLowStr = s->s;
	//検索文字列
	private String target = "";
	
	
	//文字列検索のアルゴリズムを指定する
	//デフォルトは大文字小文字を無視して検索するモード
	private Function<String,String> funcSearchStrDefualt = s->{
		if(s.indexOf(funcOnUpLowStr.apply(target))>=0)return s;
		else return "";
	};
	//デフォルトは大文字小文字を無視して検索するモード
	private Function<String,String> funcSearchStr = funcSearchStrDefualt;
	
;	@FXML 
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
		if(this.checkReg.isSelected()) {
			//正規表現を使ってマッチングする
			funcSearchStr=s->{
				if(funcOnUpLowStr.apply(s).matches(target))return s;
				else return "";
			};
		}else {
			//正規表現を使わないモードに戻す
			funcSearchStr = funcSearchStrDefualt;
		}
	}
	
	@FXML 
	//大文字小文字を無視するcheckボックスの押下状況に応じて
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
			//検索条件を設定
			target =funcOnUpLowStr.apply(searchStr.getText()==null ?"":searchStr.getText());
			new FindingStr(
				//ディレクトリ操作処理実行中にやりたいことをラムダ式で指定する
				s->resultField.setText(s + "\n" +resultField.getText() ),
				//セルの値を取得したときにやりたいことを関数型インターフェイスで指定する
				funcSearchStr
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
