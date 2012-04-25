package er.rbac.eof;

import org.apache.log4j.Logger;

public class ERRole extends _ERRole {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ERRole.class);
	
	@Override
	public String toString() {
		return name();
	}
}
