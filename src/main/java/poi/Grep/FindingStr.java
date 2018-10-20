package poi.Grep;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.poi.hssf.usermodel.HSSFShapeGroup;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFShapeGroup;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;

import poi.file.FileSearcher;

public class FindingStr   {
	//セルから値が取れた時に実行するFunctionを指定する
	private Function<String,String> exeOnGetCellValue = null;
	//セルから日付値が取れた時に実行するFunctionを指定する
	private Function<Date,String> dateFormat = null;
	//セルのGrep結果を格納するリスト
	private List<String>result = new ArrayList<String>();
	
	private FileSearcher fileSearcher;
	//pathFunc:パス走査時に実行中のパスを使う処理
	//func:セルの値を取得したいときに実行する処理
	public FindingStr(Consumer<String> pathFunc,Function<String,String> func) {
		this.fileSearcher = new FileSearcher();
		this.fileSearcher.acceptBeforeExe(pathFunc)
		.acceptMainExe(this::searchWorkBook)
		.acceptFileCon(f->f.getName().matches(".*\\.xls|.*\\.xlsx"));
		
		this.exeOnGetCellValue= func;
	}	
	
	//検索を実行する
	//path:検索対象のディレクトリ
	//outpath:検索結果の出力先
	public void search(String path,String outpath) throws Exception{
		//ラムダ式でExcelファイルからセルで日付型だった場合のフォーマットを定義する
		dateFormat=(d)->{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			return sdf.format(d);
		};
		//指定されたパス以下のディレクトリを渡り歩く
		Files.walkFileTree(Paths.get(path), fileSearcher);
		//検索結果をファイルに出力する
		Files.write(Paths.get(outpath),
				this.result,
				Charset.forName("UTF-8"),
				StandardOpenOption.CREATE);
	}

	//WorkBookの探索
	private void searchWorkBook(File  file) {
		try {
			WorkbookFactory.create(file)
			.forEach(s -> searchSheet(s));
		}catch(Exception e) {
			System.out.println("ファイル読み込み時にエラーが発生しました "+file.getName());
		}
	}
	//シートを処理するメソッド
	private void searchSheet(Sheet s) {
		//オートシェイプを処理します
		s.createDrawingPatriarch()
		.forEach(o->handleShape(o));
		//セルを処理します
		s.forEach(r->searchRow(r));
	}
	//Row(行)を処理するメソッド
	private void searchRow(Row r) {
		r.forEach(c->searchCell(c));
	}
	//セル(列)を処理するメソッド
	private void searchCell(Cell c) {
		result.add(exeOnGetCellValue.apply(getCellValue(c)));
	}
	
	//オートシェイプを処理するメソッド
	private void handleShape(Object d) {
		String s="";
		//shapeの処理(XLSX形式)
		if(d instanceof XSSFSimpleShape) {
			s =((XSSFSimpleShape) d).getText();
		}
		//shapeの処理(XLS形式)
		if(d instanceof HSSFSimpleShape) {
			s =((HSSFSimpleShape) d).getString().getString();
		}
		//グループ化されたshapeの処理(XLSX形式)
		if(d instanceof XSSFShapeGroup) {
			((XSSFShapeGroup)d).forEach(gs->handleShape(gs));
		}
		//グループ化されたshapeの処理(XLS形式)
		if(d instanceof HSSFShapeGroup) {
			((HSSFShapeGroup)d).forEach(gs->handleShape(gs));
		}
		result.add(exeOnGetCellValue.apply(s));
	}
	
	//セルのテキスト値を取得するメソッド
	private  String getCellValue(Cell c) {
		CellType t = c.getCellTypeEnum();
		if(CellType.BOOLEAN == t) {
			return c.getBooleanCellValue()?"true":"false";
		}
		if(CellType.STRING == t) {
			return c.getStringCellValue();
		}
		if(CellType.NUMERIC== t) {
			if(DateUtil.isCellDateFormatted(c)) {
				return dateFormat.apply(c.getDateCellValue());
			}else {
				return c.getNumericCellValue() + "";
			}
		}
		if(CellType.FORMULA == t) {
			//セルに関数が含まれている場合、まずは数値形式でデータを取り出す
			try {
				return c.getNumericCellValue()+"";
			}catch(Exception e) {
				//数値形式での取り出しに失敗したら文字列型でデータを取り出す
				return c.getStringCellValue();
			}
		}
		if(CellType.ERROR == t) {
			return c.getErrorCellValue() + "";
		}
		return "";
		
	}
	
}
