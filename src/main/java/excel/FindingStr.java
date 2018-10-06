package excel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.poi.hssf.usermodel.HSSFTextbox;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;

public class FindingStr  implements FileVisitor<Path> {
	//セルから値が取れた時に実行するFunctionを指定する
	private Consumer<String> func = null;
	//セルから日付値が取れた時に実行するFunctionを指定する
	private Function<Date,String> dateFormat = null;
	//セルのGrep結果を格納するリスト
	private List<String>result = new ArrayList<String>();
	//パス操作の状況をFindingStrの実行先に通知するFucntionを指定する
	private Consumer<String> parentFunc = null;
	
	public FindingStr(Consumer<String> parentFunc) {
		this.parentFunc = parentFunc;
	}	
	
	//検索を実行する
	//path:検索対象のディレクトリ
	//outpath:検索結果の出力先
	//regex:検索対象文字列
	public void search(String path,String outpath,String regex) throws Exception{
		//ラムダ式でExcelファイルからセルの値を取得した時に実行したいことを定義する
		func= (s)->{
			if(s.matches(regex))result.add(s);
		};
		//ラムダ式でExcelファイルからセルで日付型だった場合のフォーマットを定義する
		dateFormat=(d)->{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			return sdf.format(d);
		};
		//指定されたパス以下のディレクトリを渡り歩く
		Files.walkFileTree(Paths.get(path), this);
		//検索結果をファイルに出力する
		Files.write(Paths.get(outpath),
				this.result,
				Charset.forName("UTF-8"),
				StandardOpenOption.CREATE);
	}

	private void searchWorkBook(Workbook  wk) {
		wk.forEach(s -> searchSheet(s));
	}
	//シートを処理するメソッド
	private void searchSheet(Sheet s) {
		//オートシェイプを処理します
		s.createDrawingPatriarch().forEach(o->handleShape(o));
		//セルを処理します
		s.forEach(r->searchRow(r));
	}
	//Row(行)を処理するメソッド
	private void searchRow(Row r) {
		r.forEach(c->searchCell(c));
	}
	//セル(列)を処理するメソッド
	private void searchCell(Cell c) {
		func.accept(getCellValue(c));
	}
	
	//オートシェイプを処理するメソッド
	private void handleShape(Object d) {
		String s="";
		//XLSX形式の場合の処理
		if(d instanceof XSSFSimpleShape) {
			s =((XSSFSimpleShape) d).getText();
		}
		//XLS形式の場合の処理
		if(d instanceof HSSFTextbox) {
			s =((HSSFTextbox) d).getString().getString();
		}
		func.accept(s);
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
			return c.getCellFormula();
		}
		if(CellType.ERROR == t) {
			return c.getErrorCellValue() + "";
		}
		return "";
		
	}
	
	//FileVisitorのオーバーライド用のメソッド
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;	
	}

	//パスを走査時にファイルだった時の処理
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		parentFunc.accept(file.toFile().getAbsolutePath());
		
		//Excel形式以外のファイルは読み飛ばす
		if(!file.toFile().getName().matches(".*\\.xls|.*\\.xlsx")) {
			return FileVisitResult.CONTINUE;
		}
		try {
			//Excel形式のファイルの時、ブックの探索を行う
			searchWorkBook(WorkbookFactory.create(file.toFile()));
		}catch(Exception e) {
			System.out.println("ファイル読み込み時にエラーが発生しました");
		}
		return FileVisitResult.CONTINUE;	
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;	
	}

}
