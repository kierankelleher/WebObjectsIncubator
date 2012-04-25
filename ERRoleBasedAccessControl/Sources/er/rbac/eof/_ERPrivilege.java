// DO NOT EDIT.  Make changes to ERPrivilege.java instead.
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
public abstract class _ERPrivilege extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "ERPrivilege";
public static interface ERXKeys {
	  // Attribute Keys
		  public static final ERXKey<String> CATEGORY = new ERXKey<String>("category");
		  public static final ERXKey<String> DESCRIPTION = new ERXKey<String>("description");
		  public static final ERXKey<String> NAME = new ERXKey<String>("name");
		  // Relationship Keys
		  public static final ERXKey<er.rbac.eof.ERRole> ROLES = new ERXKey<er.rbac.eof.ERRole>("roles");
	}

public static interface Keys {
	  // Attributes
		  public static final String CATEGORY = ERXKeys.CATEGORY.key();
		  public static final String DESCRIPTION = ERXKeys.DESCRIPTION.key();
		  public static final String NAME = ERXKeys.NAME.key();
		  // Relationships
		  public static final String ROLES = ERXKeys.ROLES.key();
	}


  private static Logger LOG = Logger.getLogger(_ERPrivilege.class);

  public ERPrivilege localInstanceIn(EOEditingContext editingContext) {
    ERPrivilege localInstance = (ERPrivilege)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String category() {
    return (String) storedValueForKey(_ERPrivilege.Keys.CATEGORY);
  }

  public void setCategory(String value) {
    if (_ERPrivilege.LOG.isDebugEnabled()) {
    	_ERPrivilege.LOG.debug( "updating category from " + category() + " to " + value);
    }
    takeStoredValueForKey(value, _ERPrivilege.Keys.CATEGORY);
  }

  public String description() {
    return (String) storedValueForKey(_ERPrivilege.Keys.DESCRIPTION);
  }

  public void setDescription(String value) {
    if (_ERPrivilege.LOG.isDebugEnabled()) {
    	_ERPrivilege.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, _ERPrivilege.Keys.DESCRIPTION);
  }

  public String name() {
    return (String) storedValueForKey(_ERPrivilege.Keys.NAME);
  }

  public void setName(String value) {
    if (_ERPrivilege.LOG.isDebugEnabled()) {
    	_ERPrivilege.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _ERPrivilege.Keys.NAME);
  }

  public NSArray<er.rbac.eof.ERRole> roles() {
    return (NSArray<er.rbac.eof.ERRole>)storedValueForKey(_ERPrivilege.Keys.ROLES);
  }

  public NSArray<er.rbac.eof.ERRole> roles(EOQualifier qualifier) {
    return roles(qualifier, null);
  }

  public NSArray<er.rbac.eof.ERRole> roles(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.rbac.eof.ERRole> results;
      results = roles();
      if (qualifier != null) {
        results = (NSArray<er.rbac.eof.ERRole>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.rbac.eof.ERRole>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToRoles(er.rbac.eof.ERRole object) {
    includeObjectIntoPropertyWithKey(object, _ERPrivilege.Keys.ROLES);
  }

  public void removeFromRoles(er.rbac.eof.ERRole object) {
    excludeObjectFromPropertyWithKey(object, _ERPrivilege.Keys.ROLES);
  }

  public void addToRolesRelationship(er.rbac.eof.ERRole object) {
    if (_ERPrivilege.LOG.isDebugEnabled()) {
      _ERPrivilege.LOG.debug("adding " + object + " to roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRoles(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ERPrivilege.Keys.ROLES);
    }
  }

  public void removeFromRolesRelationship(er.rbac.eof.ERRole object) {
    if (_ERPrivilege.LOG.isDebugEnabled()) {
      _ERPrivilege.LOG.debug("removing " + object + " from roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRoles(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ERPrivilege.Keys.ROLES);
    }
  }

  public er.rbac.eof.ERRole createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.rbac.eof.ERRole.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ERPrivilege.Keys.ROLES);
    return (er.rbac.eof.ERRole) eo;
  }

  public void deleteRolesRelationship(er.rbac.eof.ERRole object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ERPrivilege.Keys.ROLES);
    editingContext().deleteObject(object);
  }

  public void deleteAllRolesRelationships() {
    Enumeration<er.rbac.eof.ERRole> objects = roles().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRolesRelationship(objects.nextElement());
    }
  }


  public static ERPrivilege createERPrivilege(EOEditingContext editingContext, String name
) {
    ERPrivilege eo = (ERPrivilege) EOUtilities.createAndInsertInstance(editingContext, _ERPrivilege.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static NSArray<ERPrivilege> fetchAllERPrivileges(EOEditingContext editingContext) {
    return _ERPrivilege.fetchAllERPrivileges(editingContext, null);
  }

  public static NSArray<ERPrivilege> fetchAllERPrivileges(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERPrivilege.fetchERPrivileges(editingContext, null, sortOrderings);
  }

  public static NSArray<ERPrivilege> fetchERPrivileges(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERPrivilege> fetchSpec = new ERXFetchSpecification<ERPrivilege>(_ERPrivilege.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERPrivilege> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERPrivilege fetchERPrivilege(EOEditingContext editingContext, String keyName, Object value) {
    return _ERPrivilege.fetchERPrivilege(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERPrivilege fetchERPrivilege(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERPrivilege> eoObjects = _ERPrivilege.fetchERPrivileges(editingContext, qualifier, null);
    ERPrivilege eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERPrivilege that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERPrivilege fetchRequiredERPrivilege(EOEditingContext editingContext, String keyName, Object value) {
    return _ERPrivilege.fetchRequiredERPrivilege(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERPrivilege fetchRequiredERPrivilege(EOEditingContext editingContext, EOQualifier qualifier) {
    ERPrivilege eoObject = _ERPrivilege.fetchERPrivilege(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERPrivilege that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERPrivilege localInstanceIn(EOEditingContext editingContext, ERPrivilege eo) {
    ERPrivilege localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
