package net.nodj.autoderivplugin.handlers;

import java.util.ArrayList;
import org.eclipse.core.resources.IResource;

/** Simple structure that is produced by a change delta visit. */
public class VisitData{
	boolean confAdded = false;    // set to true when a conf file is added to a Project
	boolean confDeleted = false;  // set to true when a conf file is deleted
	boolean confUpdated = false;  // set to true when a conf file is modified
	boolean projAdded = false;    // set to true when a Project is added to the workspace or opened
	boolean	projDeleted = false;  // set to true when a Project is removed to the workspace or closed

	ArrayList<IResource> added = new ArrayList<IResource>();  // list of added resources to be handled

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("VisitData : confAdded:").append(confAdded);
		sb.append(", confDeleted:").append(confDeleted);
		sb.append(", confUpdated:").append(confUpdated);
		sb.append(", projAdded:").append(projAdded);
		return sb.toString();
	}

	public boolean somethingToDo() {
		return (added.size()>0) || confUpdated || confAdded || confDeleted || projAdded || projDeleted ;
	}

	public boolean nothingToDo() { return !somethingToDo(); }
}
