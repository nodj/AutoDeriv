package autoderiv.handlers;

import java.util.ArrayList;
import org.eclipse.core.resources.IResource;

public class VisitData{
	boolean confAdded = false;
	boolean confDeleted = false;
	boolean confUpdated = false;
	boolean projAdded = false;
//	IResource confFile = null; // not useful since it is predictable for the project.
	ArrayList<IResource> added = new ArrayList<IResource>();
//	ArrayList<IResource> updated = new ArrayList<IResource>(); // osef
//	ArrayList<IResource> deleted = new ArrayList<IResource>(); // osef
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("VisitData : confAdded:").append(confAdded);
		sb.append(", confDeleted:").append(confDeleted);
		sb.append(", confUpdated:").append(confUpdated);
		sb.append(", projAdded:").append(projAdded);
		return sb.toString();
	}
}
