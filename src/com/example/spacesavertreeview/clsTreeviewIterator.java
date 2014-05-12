package com.example.spacesavertreeview;

import com.example.spacesavertreeview.clsTreeview.clsTreeNode;

public abstract class clsTreeviewIterator {
	
	private clsTreeview objTreeview;
	
	public clsTreeviewIterator (clsTreeview objTreeview) {
		this.objTreeview = objTreeview;
		
		
	}
	
	public void Execute() {
		for (clsTreeNode objTreeNode : objTreeview.getRepository().objRootNodes) {
			TouchTreeNodesRecursively(objTreeNode);
		}	
	}
	
	
	private void TouchTreeNodesRecursively(clsTreeNode objTreeNode) {
		ProcessTreeNode(objTreeNode);
		for(clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			TouchTreeNodesRecursively(objChildTreeNode);
		}
		
	}


	public abstract void ProcessTreeNode(clsTreeNode objTreeNode);

}
