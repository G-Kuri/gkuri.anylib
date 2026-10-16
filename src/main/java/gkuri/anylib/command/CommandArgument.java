package gkuri.anylib.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gkuri.anylib.exception.command.CommandException;
import gkuri.anylib.user.User;
import gkuri.anylib.utils.cast.ClassCastUtil;
import gkuri.anylib.utils.multiValue.Duo;

/**
 * コマンドに渡される引数のオブジェクトです。
 * 
 * このクラスは1度使用したら再利用できません。
 */
public class CommandArgument {
	
	boolean isUsed = false;

	private String nativeCommand;
	private String command;
	private List<Object> args;
	private Object executeUser;
	private Long interactiveThreadId;
	private List<String> options;
	private int patternNumber;
	private boolean removeOptionsByArgs = true;
	private boolean isSudo = false;
	private List<CommandArgumentBuilder> downStreams;
	private CommandResult beforeCommandResult;
	
	private Class<?>[] matchPattern = {};
	private boolean splitAndParseStringArgsCompleated = false;
	private int ignoreOptionPos = Integer.MAX_VALUE;
	
	private CommandArgument() {
		
	}
	
	public CommandArgument(String command) {
		this(User.DEFAULT_USER, command, null, new ArrayList<>(), true, List.of(), null);
		this.splitCommandAndArgs();
	}
	
	/**
	 * builder use only
	 * @param executeUser
	 * @param command
	 * @param interactiveThreadId
	 * @param args
	 */
	protected CommandArgument(Object executeUser, String command, Long interactiveThreadId, List<Object> args, boolean removeOptionsByArgs, List<CommandArgumentBuilder> downStream, CommandResult beforeCommandResult) {
		StringBuilder sb = new StringBuilder();
		if(command != null) sb.append(command);
		this.args = new ArrayList<>();
		if(args != null) {
			for(Object a : args) {
				sb.append(" ").append(a.toString());
				this.args.add(a);
			}
		}
		this.options = null;
		this.patternNumber = -1;
		this.nativeCommand = sb.toString();
		this.executeUser = executeUser;
		this.command = command;
		this.interactiveThreadId = interactiveThreadId;
		this.removeOptionsByArgs = removeOptionsByArgs;
		this.downStreams = downStream;
		this.beforeCommandResult = beforeCommandResult;
	}
	
	public static CommandArgument copyOf(CommandArgument args) {
		CommandArgument newArgs = new CommandArgument();
		newArgs.command = args.command;
		newArgs.args = List.copyOf(args.args);
		newArgs.executeUser = args.executeUser;
		newArgs.options = List.copyOf(args.options);
		newArgs.isSudo = args.isSudo;
		newArgs.interactiveThreadId = args.interactiveThreadId;
		newArgs.removeOptionsByArgs = args.removeOptionsByArgs;
		newArgs.downStreams = List.copyOf(args.downStreams);
		newArgs.beforeCommandResult = args.beforeCommandResult;
		newArgs.nativeCommand = args.nativeCommand;
		newArgs.patternNumber = args.patternNumber;
		newArgs.matchPattern = Arrays.copyOf(args.matchPattern, args.matchPattern.length);
		newArgs.splitAndParseStringArgsCompleated = args.splitAndParseStringArgsCompleated;
		
		return newArgs;
	}
	
	/**
	 * 型チェックされる前の実際に入力されたコマンドを
	 * String型で返します。
	 * @return
	 */
	public String getNativeCommand() {
		return this.nativeCommand;
	}

	/**
	 * コマンド名を返します。
	 * @return
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * indexで指定された位置の引数を返します。
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getArg(int index){
		if(index < 0 || this.args.size() <= index) return null;
		return (T) this.args.get(index);
	}

	/**
	 * indexで指定された位置の引数の型を返します。
	 * @param index
	 * @return
	 */
	public Class<?> getArgType(int index){
		if(index < 0 || this.matchPattern.length < index) return this.args.get(index).getClass();
		return this.matchPattern[index];
	}
	
	/**
	 * 型チェック時に判明した引数型のパターンを返します。
	 * @return
	 */
	public Class<?>[] getArgsTypePattern(){
		return Arrays.copyOf(this.matchPattern, this.matchPattern.length);
	}

	/**
	 * indexで指定された位置の引数をStringで返します。
	 * @param index
	 * @return
	 */
	public String getArgString(int index){
		return this.args.get(index).toString();
	}

	/**
	 * indexで指定された位置の引数をIntegerで返します。
	 * @param index
	 * @return
	 */
	public Integer getArgInteger(int index){
		if(getArgType(index) == Integer.class){
			return (Integer) this.args.get(index);
		}
		throw new ClassCastException(getArgType(index).getSimpleName() + " is not Integer type.");
	}

	/**
	 * indexで指定された位置の引数をBooleanで返します。
	 * @param index
	 * @return
	 */
	public Boolean getArgBoolean(int index){
		if(getArgType(index) == Boolean.class){
			return (Boolean) this.args.get(index);
		}
		throw new ClassCastException(getArgType(index).getSimpleName() + " is not Boolean type.");
	}
	
	/**
	 * 引数をListで返します。
	 * @return
	 */
	public List<Object> getArgs() {
		return List.copyOf(this.args);
	}
	
	/**
	 * インタラクティブIDを返します。
	 * コマンドの実行結果がインタラクティブではない場合
	 * nullが返ります。
	 * @return
	 */
	public Long getInteractiveThreadId() {
		return this.interactiveThreadId;
	}

	/**
	 * 引数が空か否かを返します。
	 * @return
	 */
	public boolean argsIsEmpty(){
		return this.argsCountIs(0);
	}
	
	/**
	 * コマンドを実行したユーザを返します。
	 * @return
	 */
	public Object getExecuteUser() {
		return this.executeUser;
	}

	/**
	 * 引数オプション全てをString配列で返します。
	 * @return
	 */
	public String[] getOptions(){
		return this.options.toArray(new String[0]);
	}

	/**
	 * optionで指定した引数オプションが存在するか否かを返します。
	 * @param option
	 * @return
	 */
	public boolean hasOption(String option){
		return this.options.contains(option);
	}

	/**
	 * optionで指定した引数オプションが存在するか否かを返します。
	 * hasOptionと違い厳格ではなく、大文字小文字の違いを無視します。
	 * @param option
	 * @return
	 */
	public boolean hasOptionIgnoreCase(String option){
		for(String o : this.options){
			if(o.equalsIgnoreCase(option)) return true;
		}
		return false;
	}

	/**
	 * 引数の数を返します。
	 * @return
	 */
	public int getArgsCount(){
		return this.args.size();
	}

	/**
	 * 引数の数が指定された数と一致しているかを返します。
	 * @param count
	 * @return
	 */
	public boolean argsCountIs(int count){
		return this.args.size() == count;
	}

	/**
	 * 引数の型のパターンがCommand.getArgsType内で
	 * 何番目に登録されたパターンかを返します。
	 * @return
	 */
	public int getArgsTypePatternNumber(){
		return this.patternNumber;
	}
	
	/**
	 * パイプ"|"で繋がれた、直前に実行されたコマンドの結果を返します。
	 * @return
	 */
	public CommandResult getBeforeCommandResult() {
		return this.beforeCommandResult;
	}
	
	void removeOptionsByArgs(boolean removeOptionsByArgs) {
		this.removeOptionsByArgs = removeOptionsByArgs;
	}

	void setSudo(boolean isSudo){
		this.isSudo = isSudo;
	}

	/**
	 * sudoコマンド経由で実行されたか否かを返します。
	 * @return
	 */
	public boolean isSudo(){
		return this.isSudo;
	}
	
	protected List<CommandArgumentBuilder> getDownStreams() {
		return this.downStreams;
	}

	private void parseOptionsByArgs(){
		if(this.options != null) return;
		if(!this.splitAndParseStringArgsCompleated) splitAndParseStringArgs();
		this.options = new ArrayList<>();
		List<Integer> optionsPos = new ArrayList<>();
		for(int i = 0; i < this.args.size(); i++){
			Object a = this.args.get(i);
			if(!(a instanceof String)) continue;

			String aString = (String) a;
			if(aString.equals("--")){
				this.ignoreOptionPos = i;
				optionsPos.add(i);
				break;
			}

			if(aString.startsWith("--")){
				this.options.add(aString.substring(2));
				optionsPos.add(i);
				continue;
			}

			if(aString.startsWith("-") && 1 < aString.length()){
				optionsPos.add(i);
				for(int j = 1; j < aString.length(); j++){
					this.options.add(String.valueOf(aString.charAt(j)));
				}
			}
		}

		if(removeOptionsByArgs){
			for(int i = optionsPos.size()-1; 0 <= i; i--){
				this.ignoreOptionPos--;
				this.args.remove(optionsPos.get(i).intValue());
			}
		}
	}
	
	protected void splitAndParseStringArgs() {
		if(this.splitAndParseStringArgsCompleated) return;
		
		for(Object o : this.args){
			if(!(o instanceof String)) return;
		}

		List<String> argsList = new ArrayList<>();
		boolean inQuotes = false;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < nativeCommand.length(); i++) {
			char c = nativeCommand.charAt(i);

			if (c == '"') {
				int count = 0;
				for(int j = i-1; 0 <= j; j--) {
					if(nativeCommand.charAt(j) != '\\') {
						break;
					}
					count++;
				}
				if(0 == count || count % 2 == 0) {
					inQuotes = !inQuotes;
					continue;
				} else if(0 < count) {
					if(0 < sb.length()) {
						sb = new StringBuilder(sb.substring(0, sb.length()-1));
					}
				}
			}
			
			if (Character.isWhitespace(c) && !inQuotes) {
				if (sb.length() > 0) {
					argsList.add(sb.toString());
					sb.setLength(0);
				}
			} else {
				sb.append(c);
			}
		}

		if (sb.length() > 0) {
			argsList.add(sb.toString());
		}

		this.args.clear();
		List<List<String>> downStreams = new ArrayList<>();
		int pipe = 0;
		for(String al : argsList){
			if(al.trim().equals("|")) {
				pipe++;
				continue;
			}
			if(pipe != 0) {
				if(downStreams.size() < pipe) {
					downStreams.add(new ArrayList<>());
				}
				downStreams.getLast().add(al);
			} else {
				this.args.add(al);
			}
		}
		
		for(List<String> ds : downStreams) {
			CommandArgumentBuilder cab = new CommandArgumentBuilder();
			String com = ds.remove(0);
			cab.setCommandAndArgs(com, ds.toArray(new Object[0]));
			this.downStreams.add(cab);
		}
		
		this.splitAndParseStringArgsCompleated = true;
		parseOptionsByArgs();
	}

	
	protected void splitCommandAndArgs() {
		if(!this.splitAndParseStringArgsCompleated) this.splitAndParseStringArgs();
		if(args.isEmpty()) return;
		this.command = args.get(0).toString();
		this.args.remove(0);
	}
	
	protected int[] tryClassCast(Command c, boolean hopeNextArg) {
		List<Class<?>[]> patterns = c.getArgsType().getArgsType();
		if(patterns == null || patterns.isEmpty()) return new int[0];
		
		List<Integer> matchPattern = new ArrayList<>();
		
		Object[] argObjArr = this.args.toArray();
		for(int i = 0; i < argObjArr.length; i++) {
			if(argObjArr[i] instanceof String) {
				Object a = c.stringArgObjectMapper((String)argObjArr[i]);
				if(a != null) argObjArr[i] = a;
			}
		}
		
		for(int i = 0; i < this.args.size(); i++) {
			Class<?>[] pattern = patterns.get(i);
			try {
				Object[] elem = castByPattern(Arrays.copyOf(pattern, this.args.size() - (hopeNextArg ? 0 : 1)), argObjArr);
				if(elem.length == 0) continue;
				matchPattern.add(i);
			} catch (IllegalArgumentException ignored) {
//				ignored.printStackTrace();
			}
		}
		
		return matchPattern.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	protected Object[] classCastArgs(Command command) {
		if(this.args.isEmpty()) return this.args.toArray();
		List<Class<?>[]> patterns = command.getArgsType().getArgsType();
		if(patterns == null) return this.args.toArray();

		if (patterns.isEmpty()) {
			return this.args.toArray();
		} else {
			List<Duo<Integer, Class<?>[]>> sortedPatterns = new ArrayList<>();
			for(int i = 0; i < patterns.size(); i++){
				sortedPatterns.add(new Duo<>(i, patterns.get(i)));
			}
			sortedPatterns.sort((a, b) -> {
				if(b.getSecond().length == a.getSecond().length) {
					for(int i = 0; i < a.getSecond().length; i++) {
						if(a.getSecond()[i] == String.class && b.getSecond()[i] != String.class) return 1;
						if(b.getSecond()[i] == String.class && a.getSecond()[i] != String.class) return -1;
						if(a.getSecond()[i] == Long.class && b.getSecond()[i] == Integer.class) return 1;
						if(b.getSecond()[i] == Long.class && a.getSecond()[i] == Integer.class) return -1;
						if(a.getSecond()[i] == Integer.class && b.getSecond()[i] != Number.class) return 1;
						if(b.getSecond()[i] == Integer.class && a.getSecond()[i] != Number.class) return -1;
						if(a.getSecond()[i] == Integer.class && b.getSecond()[i] == Double.class) return 1;
						if(b.getSecond()[i] == Integer.class && a.getSecond()[i] == Double.class) return -1;
						if(a.getSecond()[i] == LocalDateTime.class && b.getSecond()[i] != LocalDateTime.class) return 1;
						if(b.getSecond()[i] == LocalDateTime.class && a.getSecond()[i] != LocalDateTime.class) return -1;
					}
				}
				return b.getSecond().length - a.getSecond().length;
			});
			for (Duo<Integer, Class<?>[]> pattern : sortedPatterns) {
//				System.out.println("ptn : " + pattern.getFirst() + " | " + List.of(pattern.getSecond()).toString());
				if(this.args.size() < pattern.getSecond().length) continue;
				Object[] argsArr = this.args.toArray();
				for(int i = 0; i < argsArr.length; i++) {
					if(argsArr[i] instanceof String) {
						Object a = command.stringArgObjectMapper((String)argsArr[i]);
						if(a != null) argsArr[i] = a;
					}
				}
				Object[] elem = castByPattern(pattern.getSecond(), argsArr);
				if(elem == null) continue;
				if(elem.length == 0){
					this.patternNumber = -1;
					return this.args.toArray();
				}
				for (int i = 0; i < this.args.size(); i++) {
					this.args.set(i, elem[i]);
				}
				this.patternNumber = pattern.getFirst();
				this.matchPattern = pattern.getSecond();
				return this.args.toArray();
			}
		}
		throw new CommandException(5, args);
	}

	private Object[] castByPattern(Class<?>[] pattern, Object[] nativeArgs) {
		Object[] result = new Object[nativeArgs.length];

		int ptn = 0;
		boolean allOfOption = true;
		for (int i = 0; i < nativeArgs.length; i++) {

			if(pattern.length <= ptn){
				result[i] = nativeArgs[i];
				continue;
			}

			if(i < this.ignoreOptionPos && nativeArgs[i] instanceof String && nativeArgs[i].equals("--")){
				result[i] = nativeArgs[i].toString();
				continue;
			}
			if(i < this.ignoreOptionPos && nativeArgs[i] instanceof String){
				String nai = (String)nativeArgs[i];
				if(nai.startsWith("-")){
					result[i] = nai;
					continue;
				}
			}
			
			allOfOption = false;
			if(nativeArgs[i] != null) {
				Object castResult = castAndCheck(nativeArgs[i], pattern[ptn++]);
				if(castResult == null) return null;
				result[i] = castResult;
			} else {
				result[i] = null;
			}
			
		}

		if(allOfOption){
			return new Object[0];
		}
		return result;
	}

	private Object castAndCheck(Object value, Class<?> expected) {

		// すでに期待型
		Object direct = ClassCastUtil.cast(value, expected);
		if (direct != null) {
			return direct;
		}

		if(value instanceof String str) {
			if (Number.class.isAssignableFrom(expected)) {
				try {
					Number num = ClassCastUtil.stringToNumber(str);
					if(num == null || num.getClass() != expected) return null;
					return num;
				} catch (NumberFormatException ignore) {}
			}

			if (expected == Boolean.class || expected == boolean.class) {
				if(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")){
					return str.equalsIgnoreCase("true");
				}
				return null;
			}
			if (expected == Character.class || expected == char.class) {
				if (str.length() == 1) {
					return str.charAt(0);
				}
				return null;
			}
			
			if(expected == LocalDate.class) {
				LocalDate date = ClassCastUtil.stringToDate(str);
				return date;
			}
			if(expected == LocalTime.class) {
				LocalTime date = ClassCastUtil.stringToTime(str);
				return date;
			}
			if(expected == LocalDateTime.class) {
				LocalDateTime date = ClassCastUtil.stringToDateTime(str);
				return date;
			}
		}
		

		return null;
	}
	
	@Override
	public String toString() {
		return "CommandArgument[ com : %s || argsCount : %d || threadId : %s  || down stream : %s ]".formatted(command, args.size(), interactiveThreadId, downStreams);
	}
	
	public static CommandArgumentBuilder getBuilder() {
		return new CommandArgumentBuilder();
	}
}
