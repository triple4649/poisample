package poi.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileSearcher implements FileVisitor<Path>{
	//ファイルが見つかった時に最初に実行する処理
	private Consumer<String> beforeFunc = null;
	//メイン処理の事前実行条件
	private Predicate<File> fileCon = null;
	//メイン処理
	private Consumer<File> mainFunc = null;
	

	public FileSearcher acceptBeforeExe(Consumer<String> beforeFunc) {
		this.beforeFunc=beforeFunc;
		return this;
	}
	
	public FileSearcher acceptMainExe(Consumer<File> mainFunc) {
		this.mainFunc=mainFunc;
		return this;
	}
	
	public FileSearcher acceptFileCon(Predicate<File> fileCon) {
		this.fileCon=fileCon;
		return this;
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
		//前処理
		beforeFunc.accept(file.toFile().getAbsolutePath());
		//指定された形式以外のファイルは読み飛ばす
		if(!fileCon.test(file.toFile())) {
			return FileVisitResult.CONTINUE;
		}
		//メイン処理実行(目的のファイルに対してGrepを実行する)
		mainFunc.accept(file.toFile());
		return FileVisitResult.CONTINUE;	
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;	
	}


}
