package gkuri.anylib.communication.filter;

import gkuri.anylib.communication.CommunicationData;

public interface CommunicationFilter {
	
	public boolean isAllowed(CommunicationData<?> data);
	
	public boolean isAllowedDataTypeByClassName(String clazzNameString);
}
