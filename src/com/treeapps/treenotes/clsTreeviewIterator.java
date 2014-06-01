package com.treeapps.treenotes;

import com.treeapps.treenotes.clsTreeview.clsTreeNode;

public abstract class clsTreeviewIterator {
	
	private clsTreeview objTreeview;
	
	public clsTreeviewIterator (clsTreeview objTreeview) {
		this.objTreeview = objTreeview;
		
		
	}
	
	public void Execute() {
		int intLevel;
		for (clsTreeNode objTreeNode : objTreeview.getRepository().objRootNodes) {
			intLevel = 0;
			TouchTreeNodesRecursively(objTreeNode, intLevel);
		}	
	}
	
	
	private void TouchTreeNodesRecursively(clsTreeNode objTreeNode, int intLevel) {
		intLevel += 1;
		ProcessTreeNode(objTreeNode, intLevel-1);
		for(clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			TouchTreeNodesRecursively(objChildTreeNode, intLevel);
		}
		
	}


	public abstract void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel);

}
