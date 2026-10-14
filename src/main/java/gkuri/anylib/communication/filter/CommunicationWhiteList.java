package gkuri.anylib.communication.filter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gkuri.anylib.communication.CommunicationData;

public class CommunicationWhiteList implements CommunicationFilter {

	private final Set<Long> versionIds;
    private final Set<String> tokens;
    private final Set<Class<?>> dataTypes;
	
	public CommunicationWhiteList() {
		this.versionIds = ConcurrentHashMap.newKeySet();
		this.tokens = ConcurrentHashMap.newKeySet();
		this.dataTypes = ConcurrentHashMap.newKeySet();
	}
	
	@Override
	public boolean isAllowed(CommunicationData<?> data) {
		return isAllowedVersion(data.getVersionId()) &&
				isAllowedToken(data.getToken()) &&
				isAllowedDataType(data.getDataClass());
	}
	
	@Override
	public boolean isAllowedDataTypeByClassName(String clazzNameString) {
		for(Class<?> c : this.dataTypes) {
			if(c.getName().equals(clazzNameString)) return true;
		}
		return false;
	}
	
	public boolean isAllowedVersion(long versionId) {
		return this.versionIds.contains(versionId);
	}

	public boolean isAllowedToken(String token) {
		return this.tokens.contains(token);
	}

	public boolean isAllowedDataType(Class<?> dataType) {
		return this.dataTypes.contains(dataType);
	}
	
	public CommunicationWhiteList addAllowVersion(long versionId) {
		this.versionIds.add(versionId);
		return this;
	}
	
	public boolean removeAllowVersion(long versionId) {
		return this.versionIds.remove(versionId);
	}
	
	public CommunicationWhiteList addAllowToken(String token) {
		this.tokens.add(token);
		return this;
	}
	
	public boolean removeAllowToken(String token) {
		return this.tokens.remove(token);
	}
	
	public CommunicationWhiteList addAllowDataType(Class<?> dataType) {
		this.dataTypes.add(dataType);
		return this;
	}
	
	public boolean removeAllowDataType(Class<?> dataType) {
		return this.dataTypes.remove(dataType);
	}

}
