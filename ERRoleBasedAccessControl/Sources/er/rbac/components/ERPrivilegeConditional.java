package er.rbac.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXBooleanExpressionParser;
import er.rbac.ERRoleBasedAccessControl;

/**
 * @binding expression a single privilege name or a privilege boolean
 *          expression of privilege terms in a format that gets eventually
 *          converted to an appropriate qualifier. The privilege terms are
 *          treated as if they were attribute keys of the user, even though they
 *          may not be. The expression can include any user keyPath that resolves to a boolean too by the way.
 * 
 *          Example privilege boolean expressions:
 *          <code>canViewPerson OR canEditPerson</code>
 *          <code>(canViewPerson OR canEditPerson) AND NOT (canViewFood AND canEatFood)</code>
 * 
 * 
 * We don't use the 'condition' binding that is customary for Conditional components since those bindings
 * usually refer to a method keyPath whereas we specifically use a String expression whose keyPaths
 * only invoke keyPaths on the user object.
 * 
 * @author kieran
 * 
 */
public class ERPrivilegeConditional extends ERXComponent {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ERPrivilegeConditional.class);

	public ERPrivilegeConditional(WOContext context) {
		super(context);
	}

	private static interface Keys {
		final static String expression = "expression";
		final static String condition = "condition";
	}

	private Boolean _showContent;
	private String _expression;
	private KeyValueCodedUser _kvcUser;
	private EOEnterpriseObject _currentUser;
	private Boolean _hasCurrentUser;
	private Boolean _isDebuggingPrivileges;

	@Override
	public void reset() {
		_booleanExpressionParser = null;
		_currentUser = null;
		_debugTag = null;
		_expression = null;
		_hasCurrentUser = null;
		_isDebuggingPrivileges = null;
		_isTableRowsConditional = null;
		_kvcUser = null;
		_showContent = null;
		
	}

	/** @return whether to show privilege protected content or not */
	public boolean showContent() {
		if (_showContent == null) {
			_showContent = Boolean.valueOf(evaluateExpression());
			if (log.isDebugEnabled())
				log.debug("_exposeContent = " + _showContent);
		}
		return _showContent.booleanValue();
	}
	
	private String _debugTag;
	
	/** @return the tag used as a debug container for indicating privileged content */
	public String debugTag() {
		if ( _debugTag == null ) {
			_debugTag = (String)valueForBinding("debugTag");
			if (_debugTag == null) {
				if (isTableRowsConditional()) {
					_debugTag = "tbody";
				} else {
					_debugTag = "span";
				}				
			}
		}
		return _debugTag;
	}
	
	private Boolean _isTableRowsConditional;

	/** @return whether this privileged content conditional surrounds tables rows, in which case we have to use different structure for the debug elements so that is renders properly */
	public boolean isTableRowsConditional() {
		if (_isTableRowsConditional == null) {
			_isTableRowsConditional = Boolean.valueOf(booleanValueForBinding("isTableRowsConditional", false));
		}
		return _isTableRowsConditional.booleanValue();
	}
	
	public boolean notTableRowsConditional() {
		return !isTableRowsConditional();
	}

	/**
	 * @return whether the current session is debugging or inspecting privileges
	 *         conditionals on web pages.
	 */
	public boolean isDebuggingPrivileges() {
		if (_isDebuggingPrivileges == null) {
			_isDebuggingPrivileges = Boolean.valueOf(ERRoleBasedAccessControl.isInspectingPrivileges());
		}
		return _isDebuggingPrivileges.booleanValue();
	}

	/** @return the privilege expression */
	public String expression() {
		if (_expression == null) {
			_expression = (String) valueForBinding(Keys.expression);
			// Supporting this for support of the conditional binding habit/expectation
			if (_expression == null) {
				_expression = (String) valueForBinding(Keys.condition);
			}
		}
		return _expression;
	}

	/**
	 * @return the result of the privileges expression for the current user
	 */
	private boolean evaluateExpression() {
		if (!hasCurrentUser()) {
			return false;
		}
		return booleanExpressionParser().evaluateWithObject(kvcUser());
	}

	private ERXBooleanExpressionParser _booleanExpressionParser;


	public ERXBooleanExpressionParser booleanExpressionParser() {
		if (_booleanExpressionParser == null) {
			_booleanExpressionParser = new ERXBooleanExpressionParser(expression());
		}
		return _booleanExpressionParser;
	}

	/** @return KVC wrapper on the user EO to intercept privilege keys */
	private KeyValueCodedUser kvcUser() {
		if (_kvcUser == null && hasCurrentUser()) {
			_kvcUser = new KeyValueCodedUser(currentUser());
		}
		return _kvcUser;
	}

	/** @return the current user */
	private EOEnterpriseObject currentUser() {
		if (_currentUser == null) {
			_currentUser = ERRoleBasedAccessControl.currentUser();
		}
		return _currentUser;
	}

	/** @return true if we have a current user */
	private boolean hasCurrentUser() {
		if (_hasCurrentUser == null) {
			_hasCurrentUser = Boolean.valueOf( currentUser() != null );
		}
		return _hasCurrentUser.booleanValue();
	}

	/**
	 * Simple KVC wrapper on the user EO for privilege evaluation
	 *
	 */
	private static class KeyValueCodedUser implements NSKeyValueCodingAdditions {
		private final EOEnterpriseObject user;

		public KeyValueCodedUser(EOEnterpriseObject user) {
			this.user = user;
		}

		public Object valueForKey(String key) {
			if (key.startsWith(ERRoleBasedAccessControl.Constants.PRIVILEGE_PREFIX, 0)) {
				return ERRoleBasedAccessControl.hasPrivilege(user, key);
			} else {
				return user.valueForKey(key);
			}
		}

		public void takeValueForKey(Object object, String key) {
			NSKeyValueCoding.DefaultImplementation.takeValueForKey(user, object, key);

		}

		public Object valueForKeyPath(String keyPath) {
			if (log.isDebugEnabled())
				log.debug("keyPath: " + keyPath);
			if (keyPath.startsWith(ERRoleBasedAccessControl.Constants.PRIVILEGE_PREFIX, 0)) {
				boolean result = ERRoleBasedAccessControl.hasCustomPrivilege(user, keyPath);
				return result;
			} else {
				return user.valueForKeyPath(keyPath);
			}
		}

		public void takeValueForKeyPath(Object value, String keyPath) {
			NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(user, value, keyPath);

		}

	}

	/** @return whether or not to render the debug outline class */
	public boolean notDebuggingPrivileges() {
		return !isDebuggingPrivileges();
	}

	/**
	 * We override this and return true to include our debugging CSS.
	 * 
	 * @see er.extensions.components.ERXComponent#useDefaultComponentCSS()
	 */
	@Override
	protected boolean useDefaultComponentCSS() {
		return (isDebuggingPrivileges() ? true : false);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		// makes this component non-synchronizing
		return false;
	}

	 @Override
	 public boolean isStateless() {
		 // makes this component stateless
		 return true;
	 }

}