package gkuri.anylib.user;

import gkuri.anylib.exception.user.UserAuthException;

public class UserAuth implements Comparable<UserAuth>{
	
	public static final UserAuth MAX_AUTHER = new UserAuth("max auth", 2_000_000_000);
	public static final UserAuth DEFAULT_AUTH = new UserAuth("default auth", 0);
	public static final UserAuth MIN_AUTHER = new UserAuth("min auth", -2_000_000_000);
	
	private static final int CREATE_AUTH_DIFFERENCE = 0x00000100;
	
	private String authName;
	private int authLevel;
	
	public UserAuth(String authName, int authLevel) {
		this.authName = authName;
		this.authLevel = authLevel;
	}
	
	public String getAuthName() {
		return this.authName;
	}
	
	public int getAuthLevel() {
		return this.authLevel;
	}
	
	public UserAuth createUpperAuth(String authName) {
		if(MAX_AUTHER.getAuthLevel() < this.authLevel + CREATE_AUTH_DIFFERENCE) {
			throw new UserAuthException();
		}
		return new UserAuth(authName, this.authLevel + CREATE_AUTH_DIFFERENCE);
	}
	
	public UserAuth createLowerAuth(String authName) {
		if(this.authLevel + CREATE_AUTH_DIFFERENCE < MIN_AUTHER.getAuthLevel()) {
			throw new UserAuthException();
		}
		return new UserAuth(authName, this.authLevel - CREATE_AUTH_DIFFERENCE);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) return true;
		if(!(object instanceof UserAuth)) return false;
		UserAuth obj = (UserAuth) object;
		return this.authName.equals(obj.getAuthName()) && this.authLevel == obj.getAuthLevel();
	}
	
	@Override
	public int hashCode() {
		int hash = this.authLevel;
		for(int i = 0; i < this.authName.length(); i++) {
			hash += (int)this.authName.charAt(i);
		}
		return hash;
	}

	@Override
	public int compareTo(UserAuth userAuth) {
		return Integer.compare(this.authLevel, userAuth.getAuthLevel());
	}
}
