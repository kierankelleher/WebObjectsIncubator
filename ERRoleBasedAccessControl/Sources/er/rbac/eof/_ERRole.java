// DO NOT EDIT.  Make changes to ERRole.java instead.
package er.rbac.eof;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _ERRole extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "ERRole";
public static interface ERXKeys {
	  // Attribute Keys
		  public static final ERXKey<String> DESCRIPTION = new ERXKey<String>("description");
		  public static final ERXKey<String> NAME = new ERXKey<String>("name");
		  // Relationship Keys
		  public static final ERXKey<er.rbac.eof.ERPrivilege> PRIVILEGES = new ERXKey<er.rbac.eof.ERPrivilege>("privileges");
	}

public static interface Keys {
	  // Attributes
		  public static final String DESCRIPTION = ERXKeys.DESCRIPTION.key();
		  public static final String NAME = ERXKeys.NAME.key();
		  // Relationships
		  public static final String PRIVILEGES = ERXKeys.PRIVILEGES.key();
	}


  private static Logger LOG = Logger.getLogger(_ERRole.class);

  public ERRole localInstanceIn(EOEditingContext editingContext) {
    ERRole localInstance = (ERRole)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String description() {
    return (String) storedValueForKey(_ERRole.Keys.DESCRIPTION);
  }

  public void setDescription(String value) {
    if (_ERRole.LOG.isDebugEnabled()) {
    	_ERRole.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, _ERRole.Keys.DESCRIPTION);
  }

  public String name() {
    return (String) storedValueForKey(_ERRole.Keys.NAME);
  }

  public void setName(String value) {
    if (_ERRole.LOG.isDebugEnabled()) {
    	_ERRole.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _ERRole.Keys.NAME);
  }

  public NSArray<er.rbac.eof.ERPrivilege> privileges() {
    return (NSArray<er.rbac.eof.ERPrivilege>)storedValueForKey(_ERRole.Keys.PRIVILEGES);
  }

  public NSArray<er.rbac.eof.ERPrivilege> privileges(EOQualifier qualifier) {
    return privileges(qualifier, null);
  }

  public NSArray<er.rbac.eof.ERPrivilege> privileges(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.rbac.eof.ERPrivilege> results;
      results = privileges();
      if (qualifier != null) {
        results = (NSArray<er.rbac.eof.ERPrivilege>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.rbac.eof.ERPrivilege>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToPrivileges(er.rbac.eof.ERPrivilege object) {
    includeObjectIntoPropertyWithKey(object, _ERRole.Keys.PRIVILEGES);
  }

  public void removeFromPrivileges(er.rbac.eof.ERPrivilege object) {
    excludeObjectFromPropertyWithKey(object, _ERRole.Keys.PRIVILEGES);
  }

  public void addToPrivilegesRelationship(er.rbac.eof.ERPrivilege object) {
    if (_ERRole.LOG.isDebugEnabled()) {
      _ERRole.LOG.debug("adding " + object + " to privileges relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPrivileges(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ERRole.Keys.PRIVILEGES);
    }
  }

  public void removeFromPrivilegesRelationship(er.rbac.eof.ERPrivilege object) {
    if (_ERRole.LOG.isDebugEnabled()) {
      _ERRole.LOG.debug("removing " + object + " from privileges relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPrivileges(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ERRole.Keys.PRIVILEGES);
    }
  }

  public er.rbac.eof.ERPrivilege createPrivilegesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.rbac.eof.ERPrivilege.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ERRole.Keys.PRIVILEGES);
    return (er.rbac.eof.ERPrivilege) eo;
  }

  public void deletePrivilegesRelationship(er.rbac.eof.ERPrivilege object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ERRole.Keys.PRIVILEGES);
    editingContext().deleteObject(object);
  }

  public void deleteAllPrivilegesRelationships() {
    Enumeration<er.rbac.eof.ERPrivilege> objects = privileges().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePrivilegesRelationship(objects.nextElement());
    }
  }


  public static ERRole createERRole(EOEditingContext editingContext, String name
) {
    ERRole eo = (ERRole) EOUtilities.createAndInsertInstance(editingContext, _ERRole.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static NSArray<ERRole> fetchAllERRoles(EOEditingContext editingContext) {
    return _ERRole.fetchAllERRoles(editingContext, null);
  }

  public static NSArray<ERRole> fetchAllERRoles(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERRole.fetchERRoles(editingContext, null, sortOrderings);
  }

  public static NSArray<ERRole> fetchERRoles(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERRole> fetchSpec = new ERXFetchSpecification<ERRole>(_ERRole.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERRole> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERRole fetchERRole(EOEditingContext editingContext, String keyName, Object value) {
    return _ERRole.fetchERRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERRole fetchERRole(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERRole> eoObjects = _ERRole.fetchERRoles(editingContext, qualifier, null);
    ERRole eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERRole that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERRole fetchRequiredERRole(EOEditingContext editingContext, String keyName, Object value) {
    return _ERRole.fetchRequiredERRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERRole fetchRequiredERRole(EOEditingContext editingContext, EOQualifier qualifier) {
    ERRole eoObject = _ERRole.fetchERRole(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERRole that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERRole localInstanceIn(EOEditingContext editingContext, ERRole eo) {
    ERRole localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
