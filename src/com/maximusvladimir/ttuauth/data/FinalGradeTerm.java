package com.maximusvladimir.ttuauth.data;

import java.io.Serializable;
import java.util.ArrayList;

public class FinalGradeTerm implements Serializable {
	private static final long serialVersionUID = -4837365820432592643L;
	public String ID;
	public String Name;
	public String StartDate;
	public String EndDate;
	public ArrayList<FinalGradeNode> nodes;
	
	@Override
	public String toString() {
		String nodeData = "[]";
		if (nodes != null && nodes.size() > 0) {
			nodeData = "[";
			for (FinalGradeNode n : nodes) {
				nodeData += n.toString() + ", ";
			}
			nodeData = nodeData.substring(0, nodeData.length() - 2) + "]";
		}
		return "{\"ID\": \"" + ID + "\", \"Name\": \"" + Name + "\", \"StartDate\": \"" + StartDate + "\", \"EndDate\": \"" + EndDate + "\", \"Nodes\": " + nodeData + "}";
	}
}