package gkuri.anylib.user;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

public class User {
	
	public static final User DEFAULT_USER = new User();
	
	private UUID userId;
	private String userName;
	private String userHashedPassword;
	private NavigableSet<UserAuth> userAuths;
	
	private User() {
		this.userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		this.userName = "default user";
		this.userHashedPassword = null;
		this.userAuths = new TreeSet<>();
		this.userAuths.add(UserAuth.DEFAULT_AUTH);
	}
	
	public User(String userName, String userPlainPassword) {
		this(userName, userPlainPassword, UserAuth.DEFAULT_AUTH);
	}
	
	public User(String userName, String userPlainPassword, UserAuth userAuth) {
		this.userId = UUID.randomUUID();
		if(userName == null) throw new NullPointerException("user name is null.");
		this.userName = userName;
		this.userAuths = new TreeSet<>();
		if(userAuth != null) this.userAuths.add(userAuth);
		this.setPassword(userPlainPassword);
	}
	
	public User(String userId, String userName, String userHashedPassword, List<UserAuth> userAuths) {
		this(UUID.fromString(userId), userName, userHashedPassword, userAuths);
	}
	
	public User(UUID userId, String userName, String userHashedPassword, List<UserAuth> userAuths) {
		if(userId == null) throw new NullPointerException("user id is null.");
		this.userId = userId;
		if(userName == null) throw new NullPointerException("user name is null.");
		this.userName = userName;
		if(userHashedPassword == null) throw new NullPointerException("password is null.");
		this.userHashedPassword = userHashedPassword;
		if(userAuths == null) throw new NullPointerException("user auths is null.");
		this.userAuths = new TreeSet<UserAuth>(userAuths);
	}
	
	public UUID getUserId() {
		return this.userId;
	}
	
	public String getStringUserId() {
		return this.userId.toString();
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public void setUserName(String userName) {
		if(this.userHashedPassword == null) throw new UnsupportedOperationException("default user can't change name.");
		if(userName == null) throw new NullPointerException("user name is null.");
		this.userName = userName;
	}
	
	public boolean passwordMatch(String plainPassword) {
		if(this.userHashedPassword == null) return false;
		try {
			return BCrypt.checkpw(plainPassword, this.userHashedPassword);
		}catch (Exception e) {
			return false;
		}
	}
	
	public void setPassword(String pass) {
		if(this.userHashedPassword == null) throw new UnsupportedOperationException("default user can't change password.");
		if(pass == null) throw new NullPointerException("password is null.");
		this.userHashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
	}
	
	public List<UserAuth> getAuthsList(){
		return new ArrayList<UserAuth>(this.userAuths);
	}
	
	public boolean addAuth(UserAuth userAuth) {
		if (hasAuth(userAuth)) return false;
		return this.userAuths.add(userAuth);
	}
	
	public boolean hasAuth(UserAuth userAuth) {
		return this.userAuths.contains(userAuth);
	}
	
	public boolean hasAtLeast(UserAuth userAuth) {
		return !this.userAuths.isEmpty() && 0 <= this.userAuths.last().compareTo(userAuth);
	}
	
	public boolean removeAuth(UserAuth userAuth) {
		return this.userAuths.remove(userAuth);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) return true;
		if(!(object instanceof User)) return false;
		User user = (User)object;
		return user.getUserId().equals(this.userId);
	}
	
	@Override
	public int hashCode() {
		return this.userId.hashCode();
	}
}
