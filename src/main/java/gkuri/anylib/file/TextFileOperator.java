package gkuri.anylib.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import gkuri.anylib.exception.file.TextFileOperatorException;

/**
 * テキストファイルの読み書きを行うのに便利なクラスです。
 */
public class TextFileOperator extends File{
	
	private final Path path;
	private int index;
	private LinkedList<String> line;
	
	/**
	 * FileOperationクラスのインスタンスを生成します。
	 * @param filePath ファイルパス
	 */
	public TextFileOperator(String filePath) {
		super(filePath);
		this.path = Paths.get(filePath);
		this.line = new LinkedList<>();
		this.index = -1;
	}
	
	/**
	 * FileOperationクラスのインスタンスを生成します。
	 * @param dir ファイルのディレクトリパス
	 * @param fileName ファイル名
	 */
	public TextFileOperator(String dir, String fileName) {
		super(dir.replace("\\", "/").concat("/").concat(fileName));
		dir = dir.replace("\\", "/");
		this.path = Paths.get(dir.concat("/").concat(fileName));
		this.line = new LinkedList<>();
		this.index = -1;
	}
	
	/**
	 * FileOperationクラスのインスタンスを生成します。
	 * @param file Fileクラスインスタンス
	 */
	public TextFileOperator(File file) {
		super(file.getAbsolutePath());
		this.path = file.toPath();
		this.line = new LinkedList<>();
		this.index = -1;
	}
	
	/**
	 * FileOperationクラスのインスタンスを生成します。
	 * @param file Fileクラスインスタンス
	 */
	public TextFileOperator(Path path) {
		super(path.toAbsolutePath().toString());
		this.path = path;
		this.line = new LinkedList<>();
		this.index = -1;
	}
	
	/**
	 * インスタンスが参照するファイルパスを返します。
	 * @return ディレクトリ名
	 */
	public Path getFilePath() {
		return this.path;
	}
	
	/**
	 * インスタンスが現在参照している行のインデックスを返します。
	 * @return index
	 */
	public int getIndex() {
		return this.index;
	}
	
	/**
	 * インスタンスが参照する行を変更します。
	 * @param 設定したい行
	 * @return 変更できたか否か
	 */
	public boolean setIndex(int index) {
		if(this.line.size() <= index) return false;
		this.index = index;
		if(this.index < -1) this.index = -1;
		return true;
	}
	
	/**
	 * インスタンスが参照している行を次の行に移行するメソッドです。
	 * @return 次の行が有るか否か
	 */
	public boolean nextLine() {
		return setIndex(this.index + 1);
	}
	
	/**
	 * 現在参照している行の内容を返します。
	 * @return 行内容
	 */
	public String getLine() {
		if(this.line.size() == 0) throw new TextFileOperatorException(0x02);
		return this.line.get(this.index);
	}
	
	/**
	 * 末尾に行を追加します。
	 * @param text 追加したいテキスト
	 */
	public void addLine(String text) {
		String[] line = text.lines().toArray(String[]::new);
		for(String s : line) {
			this.line.add(s);
		}
		checkSize();
	}
	
	/**
	 * 現在参照している行の次に行を追加します。
	 * @param text 追加したいテキスト
	 */
	public void insertLine(String text) {
		String[] line = text.lines().toArray(String[]::new);
		int index = this.index;
		for(String s : line) {
			if(index < this.line.size()) {
				this.line.add(index++, s);
			} else {
				this.addLine(s);
			}
		}
		checkSize();
	}
	
	/**
	 * 指定されたインデックスに行を追加します。
	 * @param index 行を追加したい位置のインデックス
	 * @param text 追加したいテキスト
	 */
	public void insertLine(int index, String text) {
		String[] line = text.split("\r\n");
		for(String s : line) {
			if(index < this.line.size()) {
				this.line.add(index++, s);
			} else {
				this.addLine(s);
			}
		}
		checkSize();
	}
	
	/**
	 * インスタンスが現在参照している行のテキストを書き換えます。
	 * @param text 上書きするテキスト
	 */
	public void setLine(String text) {
		this.line.set(this.index, text);
	}

	/**
	 * 任意の行のテキストを書き換えます。
	 * @param index 上書きしたい行
	 * @param text 上書きするテキスト
	 */
	public void setLine(int index, String text) {
		this.line.set(index, text);
	}
 
	/**
	 * インスタンスが参照している行を削除します。
	 */
	public void removeLine() {
		this.line.remove(this.index);
	}
	
	/**
	 * 任意の行を削除します。
	 * @param index 削除したい行
	 */
	public void removeLine(int index) {
		this.line.remove(index);
	}
	
	/**
	 * すべての行を削除します。
	 */
	public void removeAllLine() {
		this.line.clear();
		this.index = -1;
	}
	
	/**
	 * インスタンス生成時に指定したファイルを読み込みます。
	 * @return 読み込みが出来たか否か
	 */
	public boolean loadFile() {
		File file = this.path.toFile();
		if(!file.canRead()) return false;
		
		this.line.clear();

		String line;
		try {
			BufferedReader bf = new BufferedReader(new FileReader(file.getAbsolutePath()));
			
			while ((line = bf.readLine()) != null && checkSize()) {
				addLine(line);
			}
			bf.close();
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		this.index = -1;
		return true;
	}
	
	public String getAllContent() {
		StringBuilder sb = new StringBuilder();
		for(String line : this.line) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}
	
	public Reader getReader() {
		return new StringReader(this.getAllContent());
	}

	/**
	 * 現在インスタンスに保持しているテキストを
	 * インスタンス生成時に指定したファイルに書き込みます。
	 * @throws IOException 
	 */
	public void writeFile() throws IOException {
		writeFile(this.getAllContent());
	}

	/**
	 * インスタンス生成時に指定したファイルにテキストを書き込みます。
	 * @param text 書き込みたいテキスト
	 */
	public void writeFile(String text) throws IOException {
		File file = this.path.toFile();
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}
	
	private boolean checkSize() {
		if(2_100_000_000 < this.line.size())
			throw new TextFileOperatorException(0xff);
		return true;
	}

}