package er.rbac.eof;

import org.apache.log4j.Logger;

public class ERPrivilege extends _ERPrivilege {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ERPrivilege.class);
	
	public static interface ERXKeys extends _ERPrivilege.ERXKeys {

		}

	public static interface Keys extends _ERPrivilege.Keys {

		}
	
	@Override
	public String toString() {
		if (category() != null) {
			return name() + " [" + category() + "]";
		} else {
			return name();
		}
	}
}
