package er.rbac;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wk.eofextensions.WKEOUtils;
import wk.foundation.WKFileUtilities;

import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXAssert;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.prototypes.ERPrototypes;
import er.rbac.eof.ERPrivilege;
import er.rbac.eof.ERRole;

public class ERRoleBasedAccessControl extends ERXFrameworkPrincipal {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ERRoleBasedAccessControl.class);
	
	@SuppressWarnings("rawtypes")
	public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERPrototypes.class};
	
    static {
        setUpFrameworkPrincipalClass(ERRoleBasedAccessControl.class);
    }
    
	// Using a static class here means that the constants will not get initialized when ERRoleBasedAccessControl
	// is loaded, which might be too early. They will get initialized on first reference to this inner class, 'Keys'
	// See 'Effective Java 2nd Edition', item #71
	public static interface Keys {
		// The app user (aka actor) entity
		final static String USER_ENTITY_NAME = ERXProperties.stringForKey("er.rbac.userEntity");
		
		// The flattened relationship from the user thru to the Role entity
		final static String USER_KEY_ROLES = ERXProperties.stringForKeyWithDefault("er.rbac.rolesRelationshipName", "roles");
		
		// The simple one-to-many to the join entity
		final static String USER_KEY_USERROLES = "user" + StringUtils.capitalize(USER_KEY_ROLES);
		
		final static String USER_KEY_ROLES_DEFINITION = USER_KEY_USERROLES + ".role";
		
		final static String USERROLE_ENTITY_NAME = "ERUserRole";
		final static String USERROLE_USER_JOIN_ATTRIBUTE_NAME = "idUser";
		final static String USERROLE_USER_RELATIONSHIP_NAME = "user";
		
		final static String ROLE_KEY_USERROLES = "userRoles";
		final static String ROLE_KEY_USERS_DEFINITION = ROLE_KEY_USERROLES + "." + USERROLE_USER_RELATIONSHIP_NAME;
		final static String ROLE_KEY_USERS = "users";
		
		final static String INSPECT_PRIVILEGES_KEY = "_isInspectingPrivileges";

		final static String USER_HAS_PRIVILEGE_METHOD_NAME = ERXProperties.stringForKey("er.rbac.hasPrivilgeMethodName");
	}
	
	public static interface Constants {
		final static String PRIVILEGES_FILE_NAME = ERXProperties.stringForKey("er.rbac.privileges.fileName");
		final static String PRIVILEGES_FRAMEWORK_NAME = ERXProperties.stringForKeyWithDefault("er.rbac.privileges.frameworkName", "app");
		final static String DEPRECATED = "[Deprecated]";
		
		// Provides opportunity to make this defineable by a property later on.
		final static String PRIVILEGE_PREFIX = "can";
		//final static Pattern PRIVILEGE_TOKEN_REGEX = Pattern.compile(PRIVILEGE_PREFIX + "\\w*");
		
		// Match all words, including keyPaths and treating a keyPath as a single word
		final static Pattern PRIVILEGE_TOKEN_REGEX = Pattern.compile("\\w+([.]\\w*)*");
		
		final static NSSelector HAS_PRIVILEGE_SELECTOR = (Keys.USER_HAS_PRIVILEGE_METHOD_NAME == null ? null : new NSSelector(Keys.USER_HAS_PRIVILEGE_METHOD_NAME, new Class[] {String.class}));
	}
	
	private final static String[] BOOLEAN_WORDS = new String[] { "AND", "OR", "NOT" };
	
	
	
	@Override
	public void finishInitialization() {
		ERXAssert.PRE.notNull("The property er.rbac.userEntity must be defined if you are using the ERRoleBasedAccessControl framework", Keys.USER_ENTITY_NAME);
		// Create the relationships between the app user entoty and our RBAC model
		log.info("Beginning ERRoleBasedAccessControl integration with entity named " + Keys.USER_ENTITY_NAME);
		String userJoinAttribute = ERXProperties.stringForKey("er.rbac.userJoinAttribute");
		if (userJoinAttribute == null) {
			log.info("Beginning ERRoleBasedAccessControl integration with entity named " + Keys.USER_ENTITY_NAME + " and using primary key attribute as the default join attribute.");
			addRolesRelationshipToActorEntity(Keys.USER_ENTITY_NAME);
		} else {
			log.info("Beginning ERRoleBasedAccessControl integration with entity named " + Keys.USER_ENTITY_NAME + " and join attribute named " + userJoinAttribute);
			addRolesRelationshipToActorEntity(Keys.USER_ENTITY_NAME, userJoinAttribute);
		}
		
		// Update privileges
		updatePrivileges();
		
	}
	
    private static ERRoleBasedAccessControl sharedInstance;
    public static ERRoleBasedAccessControl sharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = ERXFrameworkPrincipal.sharedInstance(ERRoleBasedAccessControl.class);
        }
        return sharedInstance;
    }
    
    public void addRolesRelationshipToActorEntity(String entityName) {
        EOEntity entity  = EOModelGroup.defaultGroup().entityNamed(entityName);
        if(entity != null && entity.primaryKeyAttributeNames().count() == 1) {
            addRolesRelationshipToActorEntity(entityName, (String) entity.primaryKeyAttributeNames().lastObject());
        } else {
            throw new IllegalArgumentException("ERRoleBasedAccessControl does not support compund primary keys. So " 
            		+ entityName + " is not suitable.");
        }
    }
    
    /**
     * We create
     * <li>plain join from user to ERUserRole many-to-many join entity
     * <li>flattened join from user to ERRole
     * <li>flattened join from ERRole to user
     * 
     * 
     * 
     * 
     * 
     * Registers a run-time relationship on the actor (user)
     * entity of your business logic. The framework needs preferences 
     * relationship to access user preferences for a specific actor. 
     * Call this method when you initialize your business logic layer. 
     * (Check BTBusinessLogic class as an example.)
     * 
     * @param  entityName  String name for your actor entity
     * @param  attributeNameToJoin  String attribute name on the actor
     *         entity; used by the relationship and typically it's the 
     *         primary key. 
     */
    public void addRolesRelationshipToActorEntity(String entityName, String attributeNameToJoin) {
    	
    	// The user entity
        EOEntity actor = EOModelGroup.defaultGroup().entityNamed(entityName);
        
        // The many to many join table entity in this framework
        EOEntity userRole = EOModelGroup.defaultGroup().entityNamed(Keys.USERROLE_ENTITY_NAME);

        
        // Create the User.userRoles relationship from <UserEntity> to ERUserRole
        log.info("Creating relationship " + Keys.USER_KEY_USERROLES);
        ERXEOAccessUtilities.createRelationship(Keys.USER_KEY_USERROLES, entityName, attributeNameToJoin, 
        		Keys.USERROLE_ENTITY_NAME, Keys.USERROLE_USER_JOIN_ATTRIBUTE_NAME, 
        		true, EOClassDescription.DeleteRuleCascade, false, false, true);
        
        // Create the ERUserRole.user relationship from ERUserRole to <UserEntity>
        // This is the reverse relationship of the previous one
        log.info("Creating relationship " + Keys.USERROLE_USER_RELATIONSHIP_NAME);
        ERXEOAccessUtilities.createRelationship(Keys.USERROLE_USER_RELATIONSHIP_NAME, Keys.USERROLE_ENTITY_NAME, Keys.USERROLE_USER_JOIN_ATTRIBUTE_NAME, 
        		entityName, attributeNameToJoin, 
        		false, EOClassDescription.DeleteRuleNullify, true, false, false);
        
        // Now create the flattened relationship from <UserEntity> to ERRole
        log.info("Creating flattened relationship " + Keys.USER_KEY_ROLES);
        ERXEOAccessUtilities.createFlattenedRelationship(Keys.USER_KEY_ROLES, entityName, 
        		Keys.USER_KEY_ROLES_DEFINITION, EOClassDescription.DeleteRuleNullify, false, true);
        
        // Finally create the reverse flattened relationship from ERRole to <UserEntity>
        log.info("Creating flattened relationship " + Keys.ROLE_KEY_USERS);
        ERXEOAccessUtilities.createFlattenedRelationship(Keys.ROLE_KEY_USERS, ERRole.ENTITY_NAME, 
        		Keys.ROLE_KEY_USERS_DEFINITION, EOClassDescription.DeleteRuleNullify, false, true);
        
        
        // TODO: Log out the relationships for the user entity and the role entity for verification.

    }
    
	/**
	 * We want to manage privileges thru a simple text file.
	 * This method reads the file, compares to Privilege table and
	 * essentially 'synchronizes'  with the file.
	 * We will update based on label key and add new ones.
	 * We will not delete ever. Existing privileges that are not in the text file list will be marked as deprecated in the description
	 * 
	 */
	public void updatePrivileges() {
		if (Constants.PRIVILEGES_FILE_NAME != null) {
			EOEditingContext ec = WKEOUtils.newManualLockingEditingContext();
			ec.lock();
			try {
				String privilegesFilePath = ERXFileUtilities.pathForResourceNamed(Constants.PRIVILEGES_FILE_NAME, Constants.PRIVILEGES_FRAMEWORK_NAME, null);
				File privilegesFile = new File(privilegesFilePath);
				
				NSMutableArray<String> updatedPrivileges = new NSMutableArray<String>();
			
				NSArray<NSDictionary<String,String>> privileges = WKFileUtilities.recordsFromCSVFile(privilegesFile);
				for (NSDictionary<String, String> privilegeRecord : privileges) {
					
					String category = privilegeRecord.objectForKey(ERPrivilege.Keys.CATEGORY);
					String description = privilegeRecord.objectForKey(ERPrivilege.Keys.DESCRIPTION);
					String name = privilegeRecord.objectForKey(ERPrivilege.Keys.NAME);
					
					ERPrivilege privilegeEO = WKEOUtils.objectMatchingKeyAndValue(ec, ERPrivilege.ENTITY_NAME, ERPrivilege.Keys.NAME, name);
					if (privilegeEO == null) {
						// Create it
						privilegeEO = ERPrivilege.createERPrivilege(ec, name);
						if (StringUtils.isNotBlank(category)) {
							privilegeEO.setCategory(category);
						}
						
						if (StringUtils.isNotBlank(description)) {
							privilegeEO.setDescription(description);
						}

						if (log.isDebugEnabled()) log.debug("Creating new Privilege = " + privilegeEO);
						updatedPrivileges.add(name);
						
					} else {
						// Update it
						if (!ObjectUtils.equals(privilegeEO.category(), category)) {
							
							if (log.isDebugEnabled()) log.debug("Updating " + privilegeEO + " category from " + privilegeEO.category() + " to " + category);
							privilegeEO.setCategory(category);
						} //~ if (!ObjectUtils)
						
						if (!ObjectUtils.equals(privilegeEO.description(), description)) {
							if (log.isDebugEnabled()) log.debug("Updating " + privilegeEO + " descriptor from " + privilegeEO.description() + " to " + description);
							privilegeEO.setDescription(description);
						} //~ if (!ObjectUtils.equals(privilegeEO.descriptor(), descriptor))
						updatedPrivileges.add(name);
					}
					ec.saveChanges();
				}
				
				// Now find all pivileges that were not updated and mark them as deprecated
				EOQualifier q = ERPrivilege.ERXKeys.NAME.notIn(updatedPrivileges);
				
				NSArray<ERPrivilege> deprecatedPrivileges = WKEOUtils.objectsMatchingQualifier(ec, ERPrivilege.ENTITY_NAME, q);
				
				for (ERPrivilege deprecatedPrivilege : deprecatedPrivileges) {
					String desc = deprecatedPrivilege.description();
					if (desc == null) {
						deprecatedPrivilege.setCategory(Constants.DEPRECATED);
					} else {
						if (!desc.contains(Constants.DEPRECATED)) {
							// Prepend it
							deprecatedPrivilege.setDescription(Constants.DEPRECATED + " " + desc);
						}
					}
					ec.saveChanges();
				}
				
				log.info("Completed updating privileges from " + privilegesFilePath);

			} finally {
				ec.unlock();
			}

		
		}
		

	}
	
	/**
	 * To avoid a stack overflow, this method should <strong>not</strong> be called from the custom hasPrivilege method in the user entity.
	 * 
	 * @param user
	 * @param privilegeName
	 * @return whether the user has the privilegeName privilege
	 */
	public static boolean hasCustomPrivilege(EOEnterpriseObject user, String privilegeName) {
		if (user == null || privilegeName == null) {
			return false;
		}
		
		if (Constants.HAS_PRIVILEGE_SELECTOR == null) {
			return hasPrivilege(user, privilegeName);
		} else {
			if (log.isDebugEnabled())
				log.debug("Calling custom method '" + Constants.HAS_PRIVILEGE_SELECTOR + " on " + user);
			Boolean hasPrivilege;
			try {
				hasPrivilege = (Boolean) Constants.HAS_PRIVILEGE_SELECTOR.invoke(user, privilegeName);
			} catch (Exception e) {
				throw new RuntimeException("Failed to invoke " + Constants.HAS_PRIVILEGE_SELECTOR + " on " + user + " with argument " + privilegeName, e);
			}
			return hasPrivilege.booleanValue();
		}
	}

	/**
	 * @param user
	 * @param privilegeName
	 * 
	 * @return true if the user has the privilege named privilegeName
	 */
	public static boolean hasPrivilege(EOEnterpriseObject user, String privilegeName) {

		NSArray<ERRole> roles = (NSArray<ERRole>) user.valueForKey(Keys.USER_KEY_ROLES);

		// CHECKME: Since we expect ERRole and ERPrivilege to be cached in memory, is it better to iterate like this rather than a fetch-spec?
		for (ERRole role : roles) {
			for (ERPrivilege privilege : role.privileges()) {
				if (privilege.name().equals(privilegeName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param user
	 * @return the unique set of privileges for a user based on the combination of roles that the user has assigned
	 */
	public static NSArray<ERPrivilege> distinctPrivilegesForUser(EOEnterpriseObject user) {
		String keyPath = ERXQ.keyPath(ERPrivilege.Keys.ROLES, Keys.ROLE_KEY_USERS);
		EOQualifier q = ERXQ.containsObject(keyPath, user);
		ERXFetchSpecification<ERPrivilege> fs = new ERXFetchSpecification<ERPrivilege>(ERPrivilege.ENTITY_NAME, q, null, true, false, null);
		return fs.fetchObjects(user.editingContext());
	}
	
	
	/**
	 * @return the current user EOEnterpriseObject. By default session().valueForKey("user") is called.
	 * 
	 * TODO: Change default implementation to check ERXThreadStorage for a key whose value is the current user EOGlobalID
	 * TODO: Implement delegate pattern for custom app logic to retrieve the user.
	 */
	public static EOEnterpriseObject currentUser() {
		try {
			return (EOEnterpriseObject) ERXSession.anySession().valueForKey("user");
		} catch (NSKeyValueCoding.UnknownKeyException e) {
			throw new RuntimeException("The method 'user' returning the current user EOEnterpriseObject needs to be defined in your Session class for Role Based Access Control feature!", e);
		}
	}
	
	/**
	 * Sets a flag for the current session that displays privilege debug info on pages wherever the component ERPrivilegeCondition is used
	 */
	public static void toggleInspectPrivileges() {
		if (isInspectingPrivileges()) {
			ERXSession.anySession().setObjectForKey(Boolean.FALSE, Keys.INSPECT_PRIVILEGES_KEY);
		} else {
			ERXSession.anySession().setObjectForKey(Boolean.TRUE, Keys.INSPECT_PRIVILEGES_KEY);
		}
	}
	
	/**
	 * @return whether the current session is debugging privileges or not.
	 */
	public static boolean isInspectingPrivileges() {
		
		// Global property
		if (ERXApplication.isDevelopmentModeSafe() && ERXProperties.booleanForKeyWithDefault("er.rbac.components.ERPrivilegeConditional.isDebugging", false)) {
			return true;
		}
		
		// Session only debugging
		WOSession sn = ERXSession.anySession();
		if (sn == null) {
			throw new IllegalStateException("No session found in thread '" + Thread.currentThread().getName() + "'!");
		}
		Boolean isInspectingPrivileges = (Boolean) sn.objectForKey(Keys.INSPECT_PRIVILEGES_KEY);
		if (isInspectingPrivileges == null) {
			isInspectingPrivileges = Boolean.FALSE;
		}
		sn.setObjectForKey(isInspectingPrivileges, Keys.INSPECT_PRIVILEGES_KEY);
		return isInspectingPrivileges.booleanValue();
	}
	
	/**
	 * @param expression, for example, canViewCustomers AND canEditUsers
	 * @return qualifier format, for example, (canViewCustomers = 'true') AND (canEditUsers = 'true')
	 */
	public static String qualifierFormatFromBooleanExpression(String expression) {
		StringBuffer sb = new StringBuffer();
		Matcher matcher = Constants.PRIVILEGE_TOKEN_REGEX.matcher(expression);
		while( matcher.find()) {
			String token = matcher.group();
			// Leave boolean word operators alone
			if (!ArrayUtils.contains(BOOLEAN_WORDS, token.toUpperCase())) {
				String replacement = "(" + matcher.group() + " = 'true')";
				matcher.appendReplacement(sb, replacement);				
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	

}
