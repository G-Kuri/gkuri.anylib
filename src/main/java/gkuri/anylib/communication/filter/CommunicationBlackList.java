package gkuri.anylib.communication.filter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gkuri.anylib.communication.CommunicationData;

public class CommunicationBlackList implements CommunicationFilter {

	private final Set<Long> versionIds;
    private final Set<String> tokens;
    private final Set<Class<?>> dataTypes;
	
	public CommunicationBlackList() {
		this.versionIds = ConcurrentHashMap.newKeySet();
		this.tokens = ConcurrentHashMap.newKeySet();
		this.dataTypes = ConcurrentHashMap.newKeySet();
	}
	
	@Override
	public boolean isAllowedDataTypeByClassName(String clazzNameString) {
		for(Class<?> c : this.dataTypes) {
			if(c.getName().equals(clazzNameString)) return false;
		}
		return true;
	}
	
	@Override
	public boolean isAllowed(CommunicationData<?> data) {
		return isAllowedVersion(data.getVersionId()) &&
				isAllowedToken(data.getToken()) &&
				isAllowedDataType(data.getDataClass());
	}
	
	public boolean isAllowedVersion(long versionId) {
		return !this.versionIds.contains(versionId);
	}

	public boolean isAllowedToken(String token) {
		return !this.tokens.contains(token);
	}

	public boolean isAllowedDataType(Class<?> dataType) {
		return !this.dataTypes.contains(dataType);
	}
	
	public CommunicationBlackList addBlockVersion(long versionId) {
		this.versionIds.add(versionId);
		return this;
	}
	
	public boolean removeBlockVersion(long versionId) {
		return this.versionIds.remove(versionId);
	}
	
	public CommunicationBlackList addBlockToken(String token) {
		this.tokens.add(token);
		return this;
	}
	
	public boolean removeBlockToken(String token) {
		return this.tokens.remove(token);
	}
	
	public CommunicationBlackList addBlockDataType(Class<?> dataType) {
		this.dataTypes.add(dataType);
		return this;
	}
	
	public boolean removeBlockDataType(Class<?> dataType) {
		return this.dataTypes.remove(dataType);
	}

}
